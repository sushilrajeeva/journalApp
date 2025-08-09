package com.sb.journalApp.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JournalResponse {
    private Long id;
    private String title;
    private String message;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;
    private Long userId;
}
