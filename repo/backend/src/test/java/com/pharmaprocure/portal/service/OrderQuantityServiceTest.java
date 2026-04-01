package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderQuantityServiceTest {

    private final OrderQuantityService service = new OrderQuantityService();

    @Test
    void calculatesRemainingToShipForPartialShipment() {
        assertEquals(6, service.remainingToShip(10, 4));
    }

    @Test
    void calculatesRemainingToReceiveForPartialReceipt() {
        assertEquals(3, service.remainingToReceive(7, 4));
    }

    @Test
    void detectsDiscrepancyWhenReceivedDiffersFromShippedOrOrdered() {
        assertTrue(service.hasDiscrepancy(10, 7, 5));
    }
}
