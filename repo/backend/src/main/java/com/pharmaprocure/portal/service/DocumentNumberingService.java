package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.entity.DocumentSequenceEntity;
import com.pharmaprocure.portal.repository.DocumentSequenceRepository;
import java.time.Clock;
import java.time.Year;
import org.springframework.dao.DataIntegrityViolationException;
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
            .orElseGet(() -> createOrReloadSequence(typeCode, year));
        sequence.setLastSequenceValue(sequence.getLastSequenceValue() + 1);
        documentSequenceRepository.save(sequence);
        return "%s-%d-%06d".formatted(typeCode, year, sequence.getLastSequenceValue());
    }

    private DocumentSequenceEntity createOrReloadSequence(String typeCode, int year) {
        DocumentSequenceEntity created = new DocumentSequenceEntity();
        created.setTypeCode(typeCode);
        created.setSequenceYear(year);
        created.setLastSequenceValue(0);
        try {
            return documentSequenceRepository.saveAndFlush(created);
        } catch (DataIntegrityViolationException ex) {
            return documentSequenceRepository.findByTypeCodeAndSequenceYear(typeCode, year)
                .orElseThrow(() -> ex);
        }
    }
}
