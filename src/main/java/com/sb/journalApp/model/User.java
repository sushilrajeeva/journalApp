package com.sb.journalApp.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id")
@ToString(exclude = {"password", "journalEntries"})
@Entity
@Table(name = "users",
        indexes = @Index(name = "ux_users_username", columnList = "username", unique = true))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 👈 match BIGSERIAL
    private Long id;

    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    /** Store a BCrypt hash here (NOT the raw password). */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Journal> journalEntries = new ArrayList<>();

}
