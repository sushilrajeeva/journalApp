package com.sb.journalApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JournalRequest {
    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String message;

    /** Optional: set/replace owner on create/update */
    @NotNull @Positive(message = "useriD MUST BE > 0")
    private Long userId;
}
