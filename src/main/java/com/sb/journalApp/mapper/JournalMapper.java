package com.sb.journalApp.mapper;

import com.sb.journalApp.dto.JournalPatchRequest;
import com.sb.journalApp.dto.JournalRequest;
import com.sb.journalApp.dto.JournalResponse;
import com.sb.journalApp.model.Journal;

public final class JournalMapper {

    private JournalMapper() {

    };

    public static JournalResponse toDto(Journal journal) {
        return JournalResponse.builder()
                .id(journal.getId())
                .title(journal.getTitle())
                .message(journal.getMessage())
                .createdAt(journal.getCreatedAt())
                .lastModifiedAt(journal.getLastModifiedAt())
                .userId(journal.getUser() != null ? journal.getUser().getId() : null)
                .build();
    }

    public static void updateEntity(Journal j, JournalRequest req) {
        j.setTitle(req.getTitle());
        j.setMessage(req.getMessage());
    }

    // PATCH = partial update (only if provided and non-blank)
    public static void patchEntity(Journal j, JournalPatchRequest req) {
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            j.setTitle(req.getTitle());
        }
        if (req.getMessage() != null && !req.getMessage().isBlank()) {
            j.setMessage(req.getMessage());
        }
    }

}
