package com.pharmaprocure.portal.audit;

import com.pharmaprocure.portal.util.MaskingUtils;
import com.pharmaprocure.portal.entity.SystemAuditEventEntity;
import com.pharmaprocure.portal.repository.SystemAuditEventRepository;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);
    private final SystemAuditEventRepository repository;

    public AuditService(SystemAuditEventRepository repository) {
        this.repository = repository;
    }

    public void record(String action, String principal) {
        String maskedPrincipal = MaskingUtils.mask(principal);
        LOGGER.info("audit action={} principal={}", action, maskedPrincipal);
        persist(action, maskedPrincipal, null);
    }

    public void record(String action, String principal, String detail) {
        String maskedPrincipal = MaskingUtils.mask(principal);
        String sanitizedDetail = MaskingUtils.sanitizeText(detail);
        LOGGER.info("audit action={} principal={} detail={}", action, maskedPrincipal, sanitizedDetail);
        persist(action, maskedPrincipal, sanitizedDetail);
    }

    private void persist(String action, String principal, String detail) {
        SystemAuditEventEntity event = new SystemAuditEventEntity();
        event.setAction(action);
        event.setActorUsername(principal == null ? "unknown" : principal);
        event.setDetail(detail);
        event.setCreatedAt(OffsetDateTime.now());
        repository.save(event);
    }
}
