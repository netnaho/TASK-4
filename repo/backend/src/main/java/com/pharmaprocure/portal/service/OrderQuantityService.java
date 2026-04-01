package com.pharmaprocure.portal.service;

import org.springframework.stereotype.Service;

@Service
public class OrderQuantityService {

    public int remainingToShip(int orderedQuantity, int shippedQuantity) {
        return Math.max(orderedQuantity - shippedQuantity, 0);
    }

    public int remainingToReceive(int shippedQuantity, int receivedQuantity) {
        return Math.max(shippedQuantity - receivedQuantity, 0);
    }

    public boolean hasDiscrepancy(int orderedQuantity, int shippedQuantity, int receivedQuantity) {
        return receivedQuantity != shippedQuantity || receivedQuantity != orderedQuantity;
    }
}
