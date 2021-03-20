package com.lab.lexicalAnalyzer.pojo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"numChar"})
public class IdentifierOrValueData {

    private final int numChar;
    private final String identifier;

}
