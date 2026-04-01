package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.dto.VersionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MetaService {

    @Value("${application.version:0.0.1}")
    private String version;

    public VersionResponse getVersion() {
        return new VersionResponse("PharmaProcure Compliance Procurement Portal", version);
    }
}
