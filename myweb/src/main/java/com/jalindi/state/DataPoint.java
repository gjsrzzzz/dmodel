package com.jalindi.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public @Data

class DataPoint {
    private @NonNull
    final List<String> values=new ArrayList<>();
    private @NonNull final String repeatKey;
    public DataPoint()
    {
        repeatKey="";
    }

    private DataPoint(String repeatKey, String value)
    {
        this.repeatKey=repeatKey;
        values.add(value);
    }
    public void add(String value)
    {
        values.add(value);
    }
}
