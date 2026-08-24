package com.hotel.controller;

import com.hotel.ultis.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MyServiceRequestControllerTest {
    @Test
    void missingStatusMeansShowAllInsteadOfThrowingNullPointerException() {
        assertNull(MyServiceRequestController.normalizeStatusFilter(null));
        assertNull(MyServiceRequestController.normalizeStatusFilter("UNKNOWN"));
    }

    @Test
    void supportedStatusIsPreserved() {
        assertEquals(Constants.SR_COMPLETED,
                MyServiceRequestController.normalizeStatusFilter(Constants.SR_COMPLETED));
    }
}
