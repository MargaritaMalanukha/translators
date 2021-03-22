package com.lab.lexicalAnalyzer.constants;

import java.util.HashMap;

public class AnalyzerTables {

    public static final String initialState = "0";
    public static final String [] F = {"2", "6", "8", "10", "11", "12", "13", "101", "102"};
    public static final String [] Fstar = {"2", "6", "8"};
    public static final String [] Ferror = {"101", "102"};

    //функция перехода из одного состояния в другое
    public static final String [][] stateTransitionFunction = {
            //id, keyword or boolval
            {"0", "Letter", "1"},
            {"1", "Letter", "1"},
            {"1", "Digit", "1"},
            {"1", "other", "2"},

            //unsigned real
            {"0", "Digit", "3"},
            {"3", "Digit", "3"},
            {"3", "dot", "4"},
            {"4", "Digit", "4"},
            {"4", "other", "6"},
            {"0", "dot", "5"},
            {"5", "Digit", "4"},

            //unsigned int
            {"3", "other", "8"},

            //signed int/real
            {"0", "sign", "7"},
            {"7", "Digit", "3"},

            //Operators (op)
            {"7", "other", "12"},
            {"0", "op", "9"},
            {"9", "op", "9"},
            {"9", "other", "12"},

            //Whitespace (ws)
            {"0", "ws", "0"},

            //Newline (nl)
            {"0", "nl", "13"},

            //Semicolon (sc)
            {"0", "sc", "10"},

            //Brackets
            {"0", "brackets", "11"},

            //Undefined
            {"0", "other", "101"},
            {"5", "other", "102"}

    };

    public static final HashMap<String, String> tableOfLanguageTokens = new HashMap<>() {{
        put("program", "keyword");
        put("int", "keyword");
        put("real", "keyword");
        put("bool", "keyword");
        put("read", "keyword");
        put("print", "keyword");
        put("for", "keyword");
        put("if", "keyword");
        put("then", "keyword");
        put("fi", "keyword");
        put("to", "keyword"); put("by", "keyword");
        put("true", "boolval"); put("false", "boolval");
        put("=", "assign_op");
        put("+", "add_op"); put("-", "add_op");
        put("*", "mult_op"); put("/", "mult_op");
        put("^", "exp_op");
        put("<", "rel_op"); put("<=", "rel_op");
        put("==", "rel_op"); put("!=", "rel_op");
        put(">=", "rel_op"); put(">", "rel_op");
        put("&&", "bool_op"); put("||", "bool_op");
        put("(", "brackets_op"); put(")", "brackets_op");
        put("{", "braces_op"); put("}", "braces_op");
        put(".", "punct"); put(",", "punct");
        put(":", "punct"); put(";", "punct");
    }};



}
