package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.entity.CheckInAuditEventEntity;
import com.pharmaprocure.portal.entity.CheckInEntity;
import com.pharmaprocure.portal.entity.CheckInRevisionEntity;
import com.pharmaprocure.portal.entity.UserEntity;
import com.pharmaprocure.portal.enums.CheckInAuditAction;
import com.pharmaprocure.portal.repository.CheckInAuditEventRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

@Service
public class CheckInAuditService {

    private final CheckInAuditEventRepository repository;

    public CheckInAuditService(CheckInAuditEventRepository repository) {
        this.repository = repository;
    }

    public void record(CheckInEntity checkIn, CheckInRevisionEntity revision, UserEntity actor, CheckInAuditAction action, String detail) {
        CheckInAuditEventEntity event = new CheckInAuditEventEntity();
        event.setCheckIn(checkIn);
        event.setRevision(revision);
        event.setActorUser(actor);
        event.setAction(action);
        event.setDetail(detail);
        event.setCreatedAt(OffsetDateTime.now());
        repository.save(event);
    }
}
