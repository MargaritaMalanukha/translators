package com.lab.lexicalAnalyzer.pojo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LexerData {

    private final int numLine;
    private final String lexeme;
    private final String token;
    private final Boolean isSuccessful;
    private final int numChar;

}
