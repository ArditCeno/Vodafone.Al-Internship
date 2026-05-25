package com.vodafone.tobi2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class MockChatModel {

    private static final Logger log = LoggerFactory.getLogger(MockChatModel.class);
    private final Random random = new Random();

    public String generate(String message, String language, List<String> history) {
        log.debug("MockLLM: generating response for '{}' (lang={})", message, language);
        String m = message.toLowerCase().trim();
        boolean isSq = "sq".equals(language) || detectLanguage(m);

        if (containsAny(m, "pershendetje", "hello", "hi", "tung", "miredita", "miremengjes", "good morning", "good evening", "ckemi")) {
            return isSq
                ? "Përshëndetje! Unë jam **TOBi2.0**, asistenti juaj inteligjent i Vodafone Albania. Si mund t'ju ndihmoj sot? Mund të më pyesni për **balancën**, **planet**, **faturat**, **internetin** ose **roaming**."
                : "Hello! I'm **TOBi2.0**, your intelligent Vodafone Albania assistant. How can I help you today? Ask me about your **balance**, **plans**, **bills**, **internet**, or **roaming**.";
        }

        if (containsAny(m, "faleminderit", "thanks", "thank you", "flm")) {
            return isSq
                ? "Ju lutem! Nëse keni nevojë për ndihmë tjetër, unë jam këtu për ju."
                : "You're welcome! If you need anything else, I'm here for you.";
        }

        if (containsAny(m, "balance", "kredi", "saldo", "sa lek", "lek", "para", "money")) {
            return isSq
                ? "**Kredia juaj aktuale**: 2,450 Lek\n\nDite te mbetura ne plan: 15 dite\n\nPer te mbushur kredite, perdorni My Vodafone App ose blini nje voucher ne dyqanet tona."
                : "**Your current balance**: 2,450 Lek\n\nRemaining days on your plan: 15 days\n\nTo top up, use the My Vodafone app or buy a voucher at any Vodafone store.";
        }

        if (containsAny(m, "fatur", "bill", "paguaj", "borxh", "invoice", "ngelur", "hua")) {
            String due = LocalDate.now().plusDays(15).toString();
            return isSq
                ? "**Fatura juaj**: 1,250 Lek\nPagesa deri me: " + due + "\n\nMund ta paguani permes My Vodafone App, vodafone.al, ose dyqaneve Vodafone."
                : "**Your bill**: 1,250 Lek\nDue by: " + due + "\n\nYou can pay via My Vodafone app, vodafone.al, or Vodafone stores.";
        }

        if (containsAny(m, "top", "mbush", "voucher", "kariko", "recharge", "hedh lek", "rimbush")) {
            return isSq
                ? "**Per te mbushur kredite:**\n1. Blini voucher ne dyqanet Vodafone\n2. Perdorni My Vodafone App\n3. Dergoni kodin e voucherit ne 140\n\nMinimumi: 100 Lek"
                : "**To top up:**\n1. Buy a voucher at Vodafone stores\n2. Use the My Vodafone app\n3. Send voucher code to 140\n\nMinimum: 100 Lek";
        }

        if (containsAny(m, "internet", "wifi", "rrjet", "data", "nuk kam", "terrnet", "net" )) {
            int used = 5 + random.nextInt(15);
            return isSq
                ? ("**Perdorimi i internetit**: " + used + "GB nga 20GB\n\n" +
                   "**Hapat per zgjidhje:**\n1. Rinisni telefonin\n2. Caktivizoni Modalitetin e Aeroplanit\n3. Dergoni SMS 'INTERNET' ne 140\n4. Kontrolloni nese keni kredi te mjaftueshme")
                : ("**Data usage**: " + used + "GB out of 20GB\n\n" +
                   "**Troubleshooting:**\n1. Restart your phone\n2. Turn off Airplane Mode\n3. Send SMS 'INTERNET' to 140\n4. Check if you have enough credit");
        }

        if (containsAny(m, "plan", "paket", "abonim", "offer", "oferta", "ndryshoj")) {
            return isSq
                ? "**Planet me te mira Vodafone:**\n\n" +
                   "**Unlimited Max** - 1,500 Lek/muaj (5G i pakufizuar, 10GB roaming BE)\n" +
                   "**Unlimited L** - 1,000 Lek/muaj (50GB 5G + rrjete sociale)\n" +
                   "**Unlimited M** - 700 Lek/muaj (20GB 5G)\n" +
                   "**Smart S** - 400 Lek/muaj (5GB 4G)\n\n" +
                   "Ndryshoni planin duke derguar 'NDRYSHO' ne 140"
                : "**Best Vodafone Plans:**\n\n" +
                   "**Unlimited Max** - 1,500 Lek/month (unlimited 5G, 10GB EU roaming)\n" +
                   "**Unlimited L** - 1,000 Lek/month (50GB 5G + social media)\n" +
                   "**Unlimited M** - 700 Lek/month (20GB 5G)\n" +
                   "**Smart S** - 400 Lek/month (5GB 4G)\n\n" +
                   "Change plan by sending 'CHANGE' to 140";
        }

        if (containsAny(m, "roaming", "abroad", "jashtë", "eu", "be", "italy", "itali", "grek", "greqi")) {
            return isSq
                ? "**Kosto Roaming:**\n• Itali, Greqi, Gjermani: 0.05 EUR/min\n• MB: 0.10 EUR/min\n• SHBA: 0.50 EUR/min\n\nPaketa Speciale BE: 300 Lek per 5GB"
                : "**Roaming costs:**\n• Italy, Greece, Germany: 0.05 EUR/min\n• UK: 0.10 EUR/min\n• USA: 0.50 EUR/min\n\nSpecial EU Package: 300 Lek for 5GB";
        }

        if (containsAny(m, "network", "status", "outage", "nderprerje")) {
            return isSq
                ? "**Rrjeti ne zonen tuaj eshte ne rregull.** Nuk ka nderprerje te raportuara."
                : "**Network in your area is normal.** No outages reported.";
        }

        if (containsAny(m, "5g", "5 g")) {
            return isSq
                ? "**5G nga Vodafone Albania** - Tirane, Durres, Vlore, Shkoder, Elbasan, Korce\nShpejtesi deri ne 1 Gbps\nPerfshihet pa pagese shtese ne planet Unlimited!"
                : "**5G from Vodafone Albania** - Tirana, Durres, Vlora, Shkodra, Elbasan, Korca\nSpeeds up to 1 Gbps\nIncluded free in all Unlimited plans!";
        }

        if (containsAny(m, "agent", "njeri", "operator", "agjent", "human", "talk to")) {
            return isSq
                ? "**Po ju lidhim me nje agjent...**\n\nKoha e pritjes: ~2 minuta\nNje agjent do t'ju kontaktoje se shpejti."
                : "**Connecting you to an agent...**\n\nWait time: ~2 minutes\nAn agent will contact you shortly.";
        }

        if (containsAny(m, "help", "ndihmë", "si", "what can")) {
            return isSq
                ? "**Une mund t'ju ndihmoj me:**\n'Sa kredi kam?'\n 'Sa eshte fatura?'\n'Nuk kam internet'\n'Cfare plani?'\n'Roaming ne Itali'\n'Fol me agjent'"
                : "**I can help with:**\n'What's my balance?'\n 'My bill amount?'\n 'No internet'\n 'Available plans'\n'Roaming in Italy'\n'Talk to agent'";
        }

        return isSq
            ? "Nuk e kuptova plotësisht pyetjen tuaj.\n\nMund të provoni:\n• **'Sa kredi kam?'**\n• **'Cilat janë planet?'**\n• **'Nuk kam internet'**\n• **'Fol me agjent'**\n\nShkruani **'ndihmë'** për të parë të gjitha opsionet."
            : "I didn't quite understand.\n\nTry:\n• **'What's my balance?'**\n• **'Show me plans'**\n• **'No internet'**\n• **'Talk to agent'**\n\nType **'help'** to see all options.";
    }

    private boolean detectLanguage(String message) {
        String lower = message.toLowerCase();
        String sqChars = "ëçî";
        for (char c : sqChars.toCharArray()) {
            if (lower.indexOf(c) >= 0) return true;
        }
        String[] sqWords = {"pershendetje", "tung", "kredi", "fatur", "mbush", "faleminderit", "ndihm", "miredita", "orë", "lek", "jam"};
        int count = 0;
        for (String w : sqWords) {
            if (lower.contains(w)) count++;
        }
        return count >= 2;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
