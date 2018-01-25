package com.jalindi.myweb;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public @Data class DataPoints {
    private final List<DataPoint> dataPoints=new ArrayList<>();
}
