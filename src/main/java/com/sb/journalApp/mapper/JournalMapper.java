package com.sb.journalApp.mapper;

import com.sb.journalApp.dto.JournalPatchRequest;
import com.sb.journalApp.dto.JournalRequest;
import com.sb.journalApp.dto.JournalResponse;
import com.sb.journalApp.model.Journal;
import org.springframework.util.StringUtils;

public final class JournalMapper {

    private JournalMapper() {

    };

    public static JournalResponse toDto(Journal j) {
        return JournalResponse.builder()
                .id(j.getId())
                .title(j.getTitle())
                .message(j.getMessage())
                .createdAt(j.getCreatedAt())
                .lastModifiedAt(j.getLastModifiedAt())
                .build();
    }

    public static void updateEntity(Journal j, JournalRequest req) {
        j.setTitle(req.getTitle());
        j.setMessage(req.getMessage());
    }

    // PATCH = partial update (only if provided and non-blank)
    public static void patchEntity(Journal j, JournalPatchRequest req) {
        if (req.getTitle() != null && StringUtils.hasText(req.getTitle())) {
            j.setTitle(req.getTitle());
        }
        if (req.getMessage() != null && StringUtils.hasText(req.getMessage())) {
            j.setMessage(req.getMessage());
        }
    }

}
