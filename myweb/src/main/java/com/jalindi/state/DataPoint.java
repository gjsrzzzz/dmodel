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
    public DataPoint(String repeatKey)
    {
        this.repeatKey=repeatKey;
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

    public void addAfter(String after, String[] newValues) {
        List<String> oldValues=new ArrayList<>(values);
        values.clear();
        boolean found=false;
        for (String oldValue : oldValues) {
            values.add(oldValue);
            if (!found && (after==null || oldValue.equals(after))) {
                addNewValues(newValues);
                found=true;
            }
        }
        if (!found) {
            addNewValues(newValues);
        }
    }

    private void addNewValues(String[] newValues) {
        for (String value : newValues) {
            values.add(value);
        }
    }

    @Override
    public String toString() {
        return repeatKey+ " : "+values.toString();
    }
}
