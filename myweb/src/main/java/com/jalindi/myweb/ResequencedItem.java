package com.jalindi.myweb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data @AllArgsConstructor
public class ResequencedItem {
    private String sourceRepeatKey;
    private String destinationRepeatKey;

    @Override
    public String toString() {
        return sourceRepeatKey + " -> " + destinationRepeatKey ;
    }
}
