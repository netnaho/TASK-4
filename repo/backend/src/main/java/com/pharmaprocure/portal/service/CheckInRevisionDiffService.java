package com.pharmaprocure.portal.service;

import com.pharmaprocure.portal.dto.CheckInDtos.UpdateCheckInRequest;
import com.pharmaprocure.portal.entity.CheckInEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class CheckInRevisionDiffService {

    public List<String> changedFields(CheckInEntity current, UpdateCheckInRequest request) {
        List<String> changed = new ArrayList<>();
        if (!Objects.equals(blankToNull(current.getCommentText()), blankToNull(request.commentText()))) {
            changed.add("commentText");
        }
        if (!Objects.equals(current.getDeviceTimestamp(), request.deviceTimestamp())) {
            changed.add("deviceTimestamp");
        }
        if (!Objects.equals(current.getLatitude(), request.latitude())) {
            changed.add("latitude");
        }
        if (!Objects.equals(current.getLongitude(), request.longitude())) {
            changed.add("longitude");
        }
        return changed;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
