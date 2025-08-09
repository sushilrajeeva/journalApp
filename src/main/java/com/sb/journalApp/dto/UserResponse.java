package com.sb.journalApp.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private List<Long> journalIds;
}
