package com.sb.journalApp.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JournalPatchRequest {
    private String title;   // optional
    private String message; // optional
}
