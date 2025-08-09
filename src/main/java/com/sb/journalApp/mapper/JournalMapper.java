package com.sb.journalApp.mapper;

import com.sb.journalApp.dto.JournalRequest;
import com.sb.journalApp.dto.JournalResponse;
import com.sb.journalApp.model.Journal;

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

}
