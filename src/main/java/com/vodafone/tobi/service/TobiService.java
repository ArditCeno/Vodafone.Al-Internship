package com.vodafone.tobi.service;

import com.vodafone.tobi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TobiService {

    private static final Logger log = LoggerFactory.getLogger(TobiService.class);
    private static final String SESSION_KEY = "tobi:session:";

    private final Map<String, ConversationSession> sessionStore = new ConcurrentHashMap<>();
    private final WatsonxService watsonxService;
    private final BillingService billingService;
    private final GenesysService genesysService;

    public TobiService(WatsonxService watsonxService,
                       BillingService billingService,
                       GenesysService genesysService) {
        this.watsonxService = watsonxService;
        this.billingService = billingService;
        this.genesysService = genesysService;
    }

    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        
        ConversationSession session = getOrCreateSession(request);
        
        ChatResponse response = new ChatResponse();
        response.setSessionId(session.getSessionId());

        try {
            String userMessage = request.getMessage().toLowerCase().trim();
            String intent = detectIntent(userMessage);
            double confidence = calculateConfidence(userMessage, intent);

            response.setIntent(intent);
            response.setConfidence(confidence);

            String reply = processIntent(intent, session.getUserId(), userMessage);
            response.setMessage(reply);
            response.setResponseType(intent);

            if (intent.equals("agent")) {
                response.setRequiresHumanAgent(true);
            }

        } catch (Exception e) {
            log.error("Chat error", e);
            response.setMessage("Ndodhi një gabim. Provoni përsëri.");
        }

        session.incrementMessageCount();
        session.updateLastActivity();
        saveSession(session);

        response.setResponseTimeMs(System.currentTimeMillis() - start);
        return response;
    }

    private double calculateConfidence(String message, String intent) {
        if (intent.equals("fallback")) return 0.3;
        if (intent.equals("greeting")) return 0.95;
        if (message.length() < 5) return 0.7;
        return 0.9;
    }

    private String detectIntent(String message) {
        if (message.contains("intern") || message.contains("praktik") || message.contains("internship"))
            return "intern";
        if (message.contains("balance") || message.contains("kredi") || message.contains("saldo") || message.contains("sa kredi") || message.contains("sa lek"))
            return "balance";
        if (message.contains("top") || message.contains("mbush") || message.contains("voucher") || message.contains("kariko"))
            return "topup";
        if (message.contains("fatur") || message.contains("bill") || message.contains("paguaj") || message.contains("borxh"))
            return "bill";
        if (message.contains("internet") || message.contains("rrjet") || message.contains("wifi") || message.contains("nuk kam") || message.contains("data"))
            return "internet";
        if (message.contains("plan") || message.contains("paket") || message.contains("ndërroj") || message.contains("abonim"))
            return "plan";
        if (message.contains("agent") || message.contains("njeri") || message.contains("fol me") || message.contains("operator") || message.contains("agjent"))
            return "agent";
        if (message.contains("përshëndetje") || message.contains("hello") || message.contains("hi") || message.contains("tung") || message.contains("mirëmëngjes") || message.contains("mirëdita"))
            return "greeting";
        if (message.contains("faleminderit") || message.contains("thank") || message.contains("mirë")) {
            return "thanks";
        }
        if (message.contains("ndihmë") || message.contains("help") || message.contains("si"))
            return "help";
        return "fallback";
    }

    private String processIntent(String intent, String userId, String userMessage) {
        return switch (intent) {
            case "intern" -> "Ky projekt eshte duke u prezantuar  nga Ardit Ceno interni i Vodafone Albania!\n\nTOBi është asistenti inteligjent i zhvilluar për të ndihmuar klientët me:\n• Kontrollin e kredisë\n• Pagesën e faturave\n• Informacion për plane dhe internet\n\nSi mund t'ju ndihmoj më shumë?";
            case "balance" -> {
                String balance = billingService.getBalance(userId);
                int days = billingService.getRemainingDays(userId);
                yield "Balance juaj aktuale: " + balance + "\n\nDite te mbetura ne plan: " + days + " dite\n\nDeshironi te beni top-up? Perdorni butonin 'Kredia' ose shkruani 'top-up'.";
            }
            case "topup" -> "Per top-up mund te:\n\n1. Dergoni SMS me kodin e voucherit ne 140\n2. Perdorni My Vodafone App\n3. Vizitoni nje dyqan Vodafone\n\nDeshironi informacion per paketat me te mira?";
            case "bill" -> {
                String bill = billingService.getBill(userId);
                yield "Fatura juaj: " + bill + "\n\nMund ta paguani ne:\n• My Vodafone App\n• vodafone.al\n• Dyqanet Vodafone\n\nDeshironi te aktivizoni pagesen automatike?";
            }
            case "internet" -> {
                String usage = billingService.getDataUsage(userId);
                yield "Perdorimi i internetit: " + usage + "\n\nProvoni keto hapa:\n1) Rinisni telefonin\n2) Kontrolloni cilesimet e rrjetit\n3) Dergoni SMS 'INTERNET' ne 140\n4) Kontrolloni nese keni mbushur planin\n\nNese problemi vazhdon, shkruani 'agjent' per ndihme.";
            }
            case "plan" -> {
                String plan = billingService.getPlan(userId);
                yield "Plani juaj aktual: " + plan + "\n\nPer te ndryshuar planin:\n• Vizitoni vodafone.al\n• Thirrni *123#\n• Shkruani 'agjent' per ndihme\n\nDeshironi te dini me shume per planet tona?";
            }
            case "agent" -> "Po ju lidh me nje agjent... \n\nKohe pritje: ~2 minuta\nAgjenti do t'ju thjerre se shpejti\n\nNderkohe, mund te vazhdoni te perdorni TOBi per pyetje te tjera.";
            case "greeting" -> "Pershendetje! Une jam TOBi, asistenti juaj dixhital.\n\nSi mund t'ju ndihmoj sot?\n\nMund te me pyesni per:\nKredine • Faturat • Internetin • Planet";
            case "thanks" -> "Ju lutem! Nese keni nevoje per ndihme tjeter, jam ketu.\n\nShkruani 'ndihme' per te pare cfare mund te bej.";
            case "help" -> "Keto jane gjerat qe mund t'ju ndihmoj:\n\n'Sa eshte kredia ime?'\n'Dua te paguaj faturen'\n'Nuk kam internet'\n'Cfare plani kam?'\n'Foli me nje agjent'\n\nZgjidhni nje nga butonat e shpejte ose shkruani pyetjen tuaj!";
            default -> "Nuk e kuptova plotesisht.\n\nMund te provoni:\n• 'Sa eshte kredia ime?'\n• 'Dua oferte per internet'\n• 'Foli me nje agjent'\n\nOse shkruani 'ndihme' per me shume opsione.";
        };
    }

    private ConversationSession getOrCreateSession(ChatRequest request) {
        String sid = request.getSessionId();
        
        if (sid != null && !sid.isEmpty()) {
            ConversationSession existing = sessionStore.get(SESSION_KEY + sid);
            if (existing != null) return existing;
        }

        String newId = UUID.randomUUID().toString();
        ConversationSession session = new ConversationSession(
            newId,
            request.getUserId(),
            request.getChannel(),
            request.getLanguage() != null ? request.getLanguage() : "sq"
        );
        saveSession(session);
        return session;
    }

    private void saveSession(ConversationSession session) {
        sessionStore.put(SESSION_KEY + session.getSessionId(), session);
    }

    public ConversationSession createSession(String userId, String channel, String language) {
        String newId = UUID.randomUUID().toString();
        ConversationSession session = new ConversationSession(
            newId,
            userId,
            channel != null ? channel : "web",
            language != null ? language : "sq"
        );
        saveSession(session);
        return session;
    }

    public ConversationSession getSession(String sessionId) {
        return sessionStore.get(SESSION_KEY + sessionId);
    }

    public void endSession(String sessionId) {
        sessionStore.remove(SESSION_KEY + sessionId);
    }
}
