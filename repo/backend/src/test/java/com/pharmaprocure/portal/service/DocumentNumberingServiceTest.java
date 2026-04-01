package com.pharmaprocure.portal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pharmaprocure.portal.entity.DocumentSequenceEntity;
import com.pharmaprocure.portal.repository.DocumentSequenceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DocumentNumberingServiceTest {

    private final DocumentSequenceRepository repository = Mockito.mock(DocumentSequenceRepository.class);

    @Test
    void generatesNextDocumentNumber() {
        DocumentSequenceEntity entity = new DocumentSequenceEntity();
        entity.setTypeCode("SOP");
        entity.setSequenceYear(2026);
        entity.setLastSequenceValue(0);
        when(repository.findByTypeCodeAndSequenceYear("SOP", 2026)).thenReturn(Optional.of(entity));
        when(repository.save(any(DocumentSequenceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentNumberingService service = new DocumentNumberingService(repository, Clock.fixed(Instant.parse("2026-01-10T00:00:00Z"), ZoneOffset.UTC));
        assertEquals("SOP-2026-000001", service.nextNumber("SOP"));
    }

    @Test
    void resetsSequenceForNewYearByUsingSeparateSequenceRow() {
        DocumentSequenceEntity entity = new DocumentSequenceEntity();
        entity.setTypeCode("SOP");
        entity.setSequenceYear(2027);
        entity.setLastSequenceValue(0);
        when(repository.findByTypeCodeAndSequenceYear("SOP", 2027)).thenReturn(Optional.of(entity));
        when(repository.save(any(DocumentSequenceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentNumberingService service = new DocumentNumberingService(repository, Clock.fixed(Instant.parse("2027-01-01T00:00:00Z"), ZoneOffset.UTC));
        assertEquals("SOP-2027-000001", service.nextNumber("SOP"));
    }
}
