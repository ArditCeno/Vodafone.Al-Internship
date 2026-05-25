package com.vodafone.tobi2.tests;

import com.vodafone.tobi2.service.VodafoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VodafoneService Tests")
class VodafoneServiceTest {

    private VodafoneService vodafoneService;

    @BeforeEach
    void setUp() {
        vodafoneService = new VodafoneService();
    }

    @Test
    @DisplayName("testGetBalance: returns '2,450 Lek'")
    void testGetBalance() {
        String result = vodafoneService.getBalance("test-user");
        assertThat(result).isEqualTo("2,450 Lek");
    }

    @Test
    @DisplayName("testGetAvailablePlans: returns 4 plans")
    void testGetAvailablePlans() {
        String result = vodafoneService.getAvailablePlans();

        assertThat(result).contains("Unlimited Max");
        assertThat(result).contains("Unlimited L");
        assertThat(result).contains("Unlimited M");
        assertThat(result).contains("Smart S");
        assertThat(result).contains("1500 Lek");
        assertThat(result).contains("1000 Lek");
        assertThat(result).contains("700 Lek");
        assertThat(result).contains("400 Lek");
    }
}
