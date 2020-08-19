package com.bkent.csvparser.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiErrorWarning {
    private String code;
    private String title;
    private String description;
    private String source;
}
