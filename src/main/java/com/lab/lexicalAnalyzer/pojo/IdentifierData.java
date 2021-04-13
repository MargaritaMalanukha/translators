package com.lab.lexicalAnalyzer.pojo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"numChar", "index", "type"})
@ToString
public class IdentifierData {

    private final int index;
    private final int numChar;
    private final String type;
    private final String identifier;

}
