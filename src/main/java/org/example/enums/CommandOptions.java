package org.example.enums;

public enum CommandOptions
{

    START("/start"),
    PULL("/pull");

    String value;

    CommandOptions(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
