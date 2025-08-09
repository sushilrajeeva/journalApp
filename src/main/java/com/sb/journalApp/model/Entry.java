package com.sb.journalApp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "content")
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "entries")
public class Entry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "text") // ⬅️ remove @Lob, keep text
    private String content;

    @CreationTimestamp @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
