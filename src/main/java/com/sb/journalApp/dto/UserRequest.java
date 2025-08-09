package com.sb.journalApp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRequest {

    @Size(max = 255)
    private String name;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z]{5,}$",
                message = "username must be only alphabets and atleast 5 letters")
    private String username;

    @NotBlank
    @Size(min = 8, message = "Password must be atleast 8 charecters")
    @Pattern(regexp = "^[\\p{Alnum}\\p{Punct}]{8,}$",
            message = "password can include letters, digits, and special characters")
    private String password; // raw in request; will be hashed in service
}
