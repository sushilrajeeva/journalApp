package com.sb.journalApp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JournalPatchRequest {
    private String title;   // optional
    private String message; // optional
    /** Optional: set/replace owner on create/update */
    @NotNull
    @Positive(message = "useriD MUST BE > 0")
    private Long userId;
}
