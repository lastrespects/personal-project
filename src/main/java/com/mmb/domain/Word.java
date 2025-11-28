package com.mmb.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Word 엔티티
 * - meaning, exampleSentence 길이 확장
 * - audioPath: 음성 파일 경로(로컬 혹은 URL)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "word")
@ToString
public class Word {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String spelling;

    @Column(length = 1000)
    private String meaning;

    @Column(length = 1000)
    private String exampleSentence;

    private String audioPath;
}
