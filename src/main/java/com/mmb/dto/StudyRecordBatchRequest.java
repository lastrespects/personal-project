package com.mmb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StudyRecordBatchRequest {
    private List<StudyRecordRequest> records;
}
