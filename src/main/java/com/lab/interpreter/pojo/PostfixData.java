package com.lab.interpreter.pojo;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class PostfixData {

    //назва ідентифікатору або значення
    private String lexeme;
    //тип константи
    private String type;
    //значення константи
    private String value;

}
