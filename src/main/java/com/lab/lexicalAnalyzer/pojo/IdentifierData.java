package com.lab.lexicalAnalyzer.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"idxId", "type", "value"})
@ToString
public class IdentifierData {

    private static final String UNDEFINED = "undefined";

    //ідентифікатор (лексема)
    private String id;
    //індекс ідентифікатора в таблиці ідентифікаторів
    private int idxId;
    //тип идентификатору, если тип не визначений, то має значення "undefined"
    private String type;
    //значення ідентифікатору, якщо не визначене, то має значення "undefined"
    private String value;

    public IdentifierData(String id, int idxId) {
        this.id = id;
        this.idxId = idxId;
        type = UNDEFINED;
        value = UNDEFINED;
    }

}
