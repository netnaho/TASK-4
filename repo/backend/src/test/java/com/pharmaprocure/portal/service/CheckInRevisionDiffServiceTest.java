package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pharmaprocure.portal.dto.CheckInDtos.UpdateCheckInRequest;
import com.pharmaprocure.portal.entity.CheckInEntity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class CheckInRevisionDiffServiceTest {

    private final CheckInRevisionDiffService service = new CheckInRevisionDiffService();

    @Test
    void detectsChangedFieldsForRevisionHighlighting() {
        CheckInEntity entity = new CheckInEntity();
        entity.setCommentText("Initial");
        entity.setDeviceTimestamp(OffsetDateTime.parse("2026-03-29T10:00:00Z"));
        entity.setLatitude(new BigDecimal("9.012300"));
        entity.setLongitude(new BigDecimal("38.761200"));

        UpdateCheckInRequest request = new UpdateCheckInRequest("Updated", OffsetDateTime.parse("2026-03-29T10:05:00Z"), new BigDecimal("9.112300"), new BigDecimal("38.761200"));
        assertEquals(3, service.changedFields(entity, request).size());
    }

    @Test
    void returnsEmptyWhenNoFieldsChanged() {
        OffsetDateTime stamp = OffsetDateTime.parse("2026-03-29T10:00:00Z");
        CheckInEntity entity = new CheckInEntity();
        entity.setCommentText("Initial");
        entity.setDeviceTimestamp(stamp);
        UpdateCheckInRequest request = new UpdateCheckInRequest("Initial", stamp, null, null);
        assertTrue(service.changedFields(entity, request).isEmpty());
    }
}
