package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.entity.CriticalActionAuditEventEntity;
import com.pharmaprocure.portal.entity.CriticalActionRequestEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.CriticalActionAuditAction;
import com.pharmaprocure.portal.repository.CriticalActionAuditEventRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

@Service
public class CriticalActionAuditService {

    private final CriticalActionAuditEventRepository repository;

    public CriticalActionAuditService(CriticalActionAuditEventRepository repository) {
        this.repository = repository;
    }

    public void record(CriticalActionRequestEntity request, UserEntity actor, CriticalActionAuditAction action, String detail) {
        CriticalActionAuditEventEntity event = new CriticalActionAuditEventEntity();
        event.setRequest(request);
        event.setActorUser(actor);
        event.setAction(action);
        event.setDetail(detail);
        event.setCreatedAt(OffsetDateTime.now());
        repository.save(event);
    }
}
