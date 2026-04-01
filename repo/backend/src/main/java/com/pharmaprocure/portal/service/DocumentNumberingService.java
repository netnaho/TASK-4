package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.entity.DocumentSequenceEntity;
import com.pharmaprocure.portal.repository.DocumentSequenceRepository;
import java.time.Clock;
import java.time.Year;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentNumberingService {

    private final DocumentSequenceRepository documentSequenceRepository;
    private final Clock clock;

    @Autowired
    public DocumentNumberingService(DocumentSequenceRepository documentSequenceRepository) {
        this(documentSequenceRepository, Clock.systemUTC());
    }

    DocumentNumberingService(DocumentSequenceRepository documentSequenceRepository, Clock clock) {
        this.documentSequenceRepository = documentSequenceRepository;
        this.clock = clock;
    }

    @Transactional
    public String nextNumber(String typeCode) {
        int year = Year.now(clock).getValue();
        DocumentSequenceEntity sequence = documentSequenceRepository.findByTypeCodeAndSequenceYear(typeCode, year)
            .orElseGet(() -> {
                DocumentSequenceEntity created = new DocumentSequenceEntity();
                created.setTypeCode(typeCode);
                created.setSequenceYear(year);
                created.setLastSequenceValue(0);
                return documentSequenceRepository.save(created);
            });
        sequence.setLastSequenceValue(sequence.getLastSequenceValue() + 1);
        documentSequenceRepository.save(sequence);
        return "%s-%d-%06d".formatted(typeCode, year, sequence.getLastSequenceValue());
    }
}
