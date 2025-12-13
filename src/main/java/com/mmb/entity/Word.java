package com.mmb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Word entity: stores the core vocabulary data for the learning app.
 */
@Entity
@Table(name = "word")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    private LocalDateTime regDate;     // created at
    private LocalDateTime updateDate;  // last updated at

    @Column(nullable = false, length = 100)
    private String spelling;           // word spelling (English)

    @Column(nullable = false, length = 255)
    private String meaning;            // meaning (Korean or explanation)

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;    // example sentence

    private String audioPath;          // TTS audio file path
}
