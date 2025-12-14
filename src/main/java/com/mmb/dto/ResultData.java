package com.mmb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResultData<DT> {
    private String rsCode;
    private String rsMsg;
    private DT rsData;

    public ResultData(String rsCode, String rsMsg) {
        this(rsCode, rsMsg, null);
    }

    public ResultData(String rsCode, String rsMsg, DT rsData) {
        this.rsCode = rsCode;
        this.rsMsg = rsMsg;
        this.rsData = rsData;
    }

    public static <DT> ResultData<DT> from(String rsCode, String rsMsg) {
        return new ResultData<>(rsCode, rsMsg);
    }

    public static <DT> ResultData<DT> from(String rsCode, String rsMsg, DT rsData) {
        return new ResultData<>(rsCode, rsMsg, rsData);
    }

    public boolean isSuccess() {
        return this.rsCode != null && this.rsCode.startsWith("S-");
    }

    public boolean isFail() {
        return !this.isSuccess();
    }

    @JsonProperty("resultCode")
    public String getResultCode() {
        return this.rsCode;
    }

    @JsonProperty("msg")
    public String getMsg() {
        return this.rsMsg;
    }

    @JsonProperty("data")
    public DT getData() {
        return this.rsData;
    }
}
