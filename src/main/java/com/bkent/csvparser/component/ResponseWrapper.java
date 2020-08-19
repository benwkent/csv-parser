package com.bkent.csvparser.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseWrapper {
    private MetaData meta;
    private String data;
    private String statusCode;
    private List<ApiErrorWarning> errors;
    private List<ApiErrorWarning> warnings;
}
