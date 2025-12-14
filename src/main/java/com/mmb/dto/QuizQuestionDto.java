package com.mmb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionDto {

    private Integer wordId;
    private String type;
    private String question;
    private String correctAnswer;
    private String answer;     // synonym for correctAnswer when needed
    private String correct;    // direct correct text for compatibility
    private String[] options;
    private String prompt;    // actual question text for the UI
    private String audioPath; // optional TTS audio path
    private String spelling;  // base spelling for certain question types
    private String meaning;   // meaning text when needed

    // Quiz direction (EN_TO_KO, KO_TO_EN, etc.)
    private String direction;

    // Example sentence or hint for rendering
    private String example;
}
