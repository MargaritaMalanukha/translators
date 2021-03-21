package com.lab.lexicalAnalyzer.pojo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"numChar"})
@ToString
public class IdentifierData {

    private final int numChar;
    private final String identifier;

}
