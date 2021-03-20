package com.lab.lexicalAnalyzer.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LexerData {

    private final int numLine;
    private final String lexeme;
    private final String token;
    private final Boolean isSuccessful;

}
