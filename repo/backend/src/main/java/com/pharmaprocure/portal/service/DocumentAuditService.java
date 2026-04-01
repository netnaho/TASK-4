package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.entity.DocumentAuditEventEntity;
import com.pharmaprocure.portal.entity.DocumentEntity;
import com.pharmaprocure.portal.entity.DocumentVersionEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.DocumentAuditAction;
import com.pharmaprocure.portal.repository.DocumentAuditEventRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

@Service
public class DocumentAuditService {

    private final DocumentAuditEventRepository repository;

    public DocumentAuditService(DocumentAuditEventRepository repository) {
        this.repository = repository;
    }

    public void record(DocumentEntity document, DocumentVersionEntity version, UserEntity actor, DocumentAuditAction action, String detail) {
        DocumentAuditEventEntity event = new DocumentAuditEventEntity();
        event.setDocument(document);
        event.setVersion(version);
        event.setActorUser(actor);
        event.setAction(action);
        event.setDetail(detail);
        event.setCreatedAt(OffsetDateTime.now());
        repository.save(event);
    }
}
