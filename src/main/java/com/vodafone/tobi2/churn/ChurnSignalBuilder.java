package com.vodafone.tobi2.churn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ChurnSignalBuilder {

    private static final Logger log = LoggerFactory.getLogger(ChurnSignalBuilder.class);

    private final Map<String, ChurnSignal> mockSignals;

    public ChurnSignalBuilder() {
        mockSignals = new LinkedHashMap<>();
        mockSignals.put("VF-230510", new ChurnSignal("VF-230510", 350.0, 900.0, 2, true, 10, 20, true));
        mockSignals.put("VF-123456", new ChurnSignal("VF-123456", 800.0, 850.0, 0, false, 2, 120, false));
        mockSignals.put("VF-789012", new ChurnSignal("VF-789012", 100.0, 950.0, 4, true, 20, 10, true));
        mockSignals.put("VF-345678", new ChurnSignal("VF-345678", 500.0, 520.0, 1, true, 8, 45, false));
        mockSignals.put("VF-901234", new ChurnSignal("VF-901234", 600.0, 610.0, 0, false, 1, 180, false));
    }

    public ChurnSignal build(String userId) {
        log.info("Building churn signal for user {}", userId);

        ChurnSignal signal = mockSignals.get(userId);
        if (signal != null) return signal;

        return new ChurnSignal(userId, 350.0, 900.0, 2, true, 10, 20, true);
    }

    public Map<String, ChurnSignal> getAllMockSignals() {
        return mockSignals;
    }
}
