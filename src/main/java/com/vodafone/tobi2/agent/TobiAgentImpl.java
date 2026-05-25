package com.vodafone.tobi2.agent;

import com.vodafone.tobi2.memory.PersistentChatMemoryStore;
import com.vodafone.tobi2.recommender.PlanRecommenderService;
import com.vodafone.tobi2.recommender.PlanScore;
import com.vodafone.tobi2.recommender.UserUsageProfile;
import com.vodafone.tobi2.service.MockChatModel;
import com.vodafone.tobi2.service.translation.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TobiAgentImpl implements TobiAgent {

    private static final Logger log = LoggerFactory.getLogger(TobiAgentImpl.class);
    private final MockChatModel chatModel;
    private final PersistentChatMemoryStore memoryStore;
    private final TranslationService translationService;
    private final PlanRecommenderService planRecommender;
    private final Map<String, String> sessionLang = new ConcurrentHashMap<>();
    private final Map<String, String> pendingMessages = new ConcurrentHashMap<>();
    private final Map<String, String> lastMessageMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> repeatCounterMap = new ConcurrentHashMap<>();
    private final boolean useGoogle;

    public TobiAgentImpl(PersistentChatMemoryStore memoryStore,
                         TranslationService translationService,
                         PlanRecommenderService planRecommender,
                         @Value("${tobi2.translation.provider:mock}") String provider) {
        this.memoryStore = memoryStore;
        this.translationService = translationService;
        this.planRecommender = planRecommender;
        this.chatModel = new MockChatModel();
        this.useGoogle = "google".equals(provider);
    }

    @Override
    public String chat(String sessionId, String userId, String message, String language) {
        log.debug("TOBi2 chat: session={}, lang={}, msg={}", sessionId, language, message);

        String cleanedMsg = message.trim().toLowerCase();
        String previousMsg = lastMessageMap.get(sessionId);

        if (cleanedMsg.equals(previousMsg)) {
            int count = repeatCounterMap.merge(sessionId, 1, Integer::sum);
            if (count >= 3) {
                repeatCounterMap.remove(sessionId);
                lastMessageMap.remove(sessionId);
                sessionLang.remove(sessionId);
                pendingMessages.remove(sessionId);
                memoryStore.deleteSession(sessionId);

                return "sq".equals(language)
                        ? "Keni dërguar të njëjtin mesazh 3 herë radhazi. Po ju kaloj automatikisht te një agjent live i Vodafone për t'ju ndihmuar!"
                        : " You have sent the same message 3 times in a row. Transferring you automatically to a live Vodafone agent!";
            }
        } else {
            lastMessageMap.put(sessionId, cleanedMsg);
            repeatCounterMap.put(sessionId, 1);
        }

        if (useGoogle) {
            return chatWithGoogle(sessionId, userId, message, language);
        }
        return chatWithMock(sessionId, userId, message, language);
    }

    private String chatWithGoogle(String sessionId, String userId, String message, String language) {
        String userLang = resolveLanguage(sessionId, message);
        String englishInput = translationService.translate(message, userLang, "en");

        memoryStore.addMessage(sessionId, "user", message);
        String englishResponse = chatModel.generate(englishInput, "en", memoryStore.getHistory(sessionId));
        String response = translationService.translate(englishResponse, "en", userLang);
        memoryStore.addMessage(sessionId, "assistant", response);
        return response;
    }

    private String chatWithMock(String sessionId, String userId, String message, String language) {
        List<String> history = memoryStore.getHistory(sessionId);
        String lang = sessionLang.get(sessionId);

        if (lang == null && !history.isEmpty()) {
            lang = detectLanguageFromHistory(history);
            if (lang != null) sessionLang.put(sessionId, lang);
        }

        if (lang == null) {
            if (history.isEmpty()) {
                pendingMessages.put(sessionId, message);
                String langMsg = "Përshëndetje! Zgjidhni gjuhën tuaj / Choose your language:\n\n" +
                        "**Shtypni 1** për Shqip\n" +
                        "**Press 2** for English";
                memoryStore.addMessage(sessionId, "assistant", langMsg);
                return langMsg;
            }
            String trimmed = message.trim();
            if (trimmed.equals("1") || trimmed.equals("2")) {
                lang = trimmed.equals("1") ? "sq" : "en";
                sessionLang.put(sessionId, lang);
                memoryStore.addMessage(sessionId, "user", message);
                String pending = pendingMessages.remove(sessionId);
                if (pending != null && !pending.trim().isEmpty()) {
                    memoryStore.addMessage(sessionId, "user", pending);
                    String pendingResponse = chatModel.generate(pending, lang, history);
                    String confirm = lang.equals("sq")
                            ? "**Shqip u zgjodh!**\n\n" + pendingResponse
                            : "**English selected!**\n\n" + pendingResponse;
                    memoryStore.addMessage(sessionId, "assistant", confirm);
                    return confirm;
                }
                String confirm = lang.equals("sq")
                        ? "**Shqip u zgjodh!** Si mund t'ju ndihmoj?"
                        : "**English selected!** How can I help you?";
                memoryStore.addMessage(sessionId, "assistant", confirm);
                return confirm;
            }
            lang = detectLanguage(message);
            sessionLang.put(sessionId, lang);
            memoryStore.addMessage(sessionId, "user", message);
            String planRec = tryPlanRecommendation(message, lang, sessionId);
            if (planRec != null) {
                memoryStore.addMessage(sessionId, "assistant", planRec);
                return planRec;
            }
            String response = chatModel.generate(message, lang, history);
            memoryStore.addMessage(sessionId, "assistant", response);
            return response;
        }

        memoryStore.addMessage(sessionId, "user", message);
        String planRec = tryPlanRecommendation(message, lang, sessionId);
        if (planRec != null) {
            memoryStore.addMessage(sessionId, "assistant", planRec);
            return planRec;
        }
        String response = chatModel.generate(message, lang, memoryStore.getHistory(sessionId));
        memoryStore.addMessage(sessionId, "assistant", response);
        return response;
    }

    private String resolveLanguage(String sessionId, String message) {
        String cached = sessionLang.get(sessionId);
        if (cached != null) return cached;
        String detected = translationService.detectLanguage(message);
        sessionLang.put(sessionId, detected);
        log.debug("Auto-detected language: {} for session {}", detected, sessionId);
        return detected;
    }

    private String detectLanguageFromHistory(List<String> history) {
        int sqCount = 0;
        int enCount = 0;
        for (String h : history) {
            if (!h.startsWith("user: ")) continue;
            String msg = h.substring(6).trim();
            if (msg.equals("1") || msg.equalsIgnoreCase("shqip") || msg.equalsIgnoreCase("albanian")) return "sq";
            if (msg.equals("2") || msg.equalsIgnoreCase("anglisht") || msg.equalsIgnoreCase("english")) return "en";
            if ("sq".equals(detectLanguage(msg))) sqCount++; else enCount++;
        }
        if (sqCount > enCount) return "sq";
        if (enCount > sqCount) return "en";
        return null;
    }

    private static final List<String> PLAN_INTENTS = List.of(
        "plan", "plane", "oferta", "ofertë", "kursej", "ndryshoj", "rekomando",
        "recommend", "suggestion", "upgrade", "downgrade", "change plan",
        "cili plan", "which plan", "çfarë plani", "sa lek", "më lirë",
        "me lire", "cheaper", "best plan", "me i mire", "më i mirë"
    );

    private boolean isPlanIntent(String message) {
        String m = message.toLowerCase();
        return PLAN_INTENTS.stream().anyMatch(m::contains);
    }

    private UserUsageProfile buildProfile(String userId) {
        return new UserUsageProfile(
            userId, "unlimited-max", 1500,
            350, 400, 200,
            3, 0.25,
            true, true, "Tirane"
        );
    }

    private String tryPlanRecommendation(String message, String lang, String sessionId) {
        if (!isPlanIntent(message)) return null;
        UserUsageProfile profile = buildProfile(sessionId);
        List<PlanScore> scores = planRecommender.recommend(profile);
        if (scores.isEmpty()) return null;
        log.info("Plan recommendation triggered for session {}", sessionId);
        return formatRecommendations(lang, scores);
    }

    private String formatRecommendations(String lang, List<PlanScore> scores) {
        boolean sq = "sq".equals(lang);
        StringBuilder sb = new StringBuilder();
        if (sq) {
            sb.append("Bazuar në përdorimin tuaj, këto janë planet më të mira për ju:\n\n");
        } else {
            sb.append("Based on your usage, here are the best plans for you:\n\n");
        }

        for (PlanScore ps : scores) {
            if (ps.recommended()) {
                sb.append("⭐ **").append(ps.planName()).append("**");
                if (ps.monthlySavingsLek() > 0) {
                    sb.append(" — **kurseni ").append((int) ps.monthlySavingsLek()).append(" Lek/muaj**");
                }
            } else {
                sb.append("• **").append(ps.planName()).append("**");
            }
            sb.append("\n");
            sb.append("  ").append((int) ps.priceLek()).append(" Lek/muaj, ");

            String reason;
            if (sq) {
                reason = switch (ps.reasonKey()) {
                    case "reason.save_money" -> "kursim i madh";
                    case "reason.stop_overage" -> "më shumë të dhëna";
                    case "reason.unlock_5g" -> "përfshin 5G";
                    case "reason.roaming_included" -> "përfshin roaming";
                    default -> "përshtatje më e mirë";
                };
            } else {
                reason = switch (ps.reasonKey()) {
                    case "reason.save_money" -> "great savings";
                    case "reason.stop_overage" -> "more data";
                    case "reason.unlock_5g" -> "includes 5G";
                    case "reason.roaming_included" -> "includes roaming";
                    default -> "better fit";
                };
            }
            sb.append((int) ps.fitPercent()).append("% përputhje, ").append(reason).append("\n\n");
        }

        if (sq) {
            sb.append("Një nga planet e mësipërme mund t'ju kursejë para duke përmbushur nevojat tuaja. ");
            sb.append("Doni të kaloni në njërin prej tyre?");
        } else {
            sb.append("One of these plans can save you money while meeting your needs. ");
            sb.append("Would you like to switch to any of them?");
        }
        return sb.toString();
    }

    private String detectLanguage(String message) {
        String m = message.toLowerCase();
        String[] sqWords = {"pershendetje", "tungjatjeta", "përshëndetje", "kredi", "fatur", "mbush",
                "faleminderit", "ndihm", "miredita", "mirëdita", "lek", "jam", "shqip",
                "paguaj", "borxh", "kariko", "oferta", "planin", "abonim", "rrjet", "miremengjes",
                "mirmengjes", "natemire", "natën", "tung", "çfarë", "cfare", "cilat", "sa"};
        for (String w : sqWords) {
            if (m.contains(w)) return "sq";
        }
        if (m.contains("ë") || m.contains("ç")) return "sq";
        return "en";
    }
}