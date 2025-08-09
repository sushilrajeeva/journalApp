package com.sb.journalApp.dto;

import jakarta.validation.constraints.NotBlank;
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
}
