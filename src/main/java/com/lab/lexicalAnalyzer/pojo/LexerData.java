package com.lab.lexicalAnalyzer.pojo;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class LexerData {

    private int numLine;
    private String lexeme;
    private String token;
    private int numChar;

    public LexerData(String lexeme, String token) {
        this.lexeme = lexeme;
        this.token = token;
    }

}
