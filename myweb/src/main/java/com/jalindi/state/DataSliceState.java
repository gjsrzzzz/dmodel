package com.jalindi.state;

import com.jalindi.myweb.DataState;

import java.util.*;

public class DataSliceState {
    private final int version;
    private final Map<String, Map<String, DataPoint>> dataPoints = new HashMap<>();

    public DataSliceState() {
        version = 0;
    }

    public DataSliceState(int version) {
        this.version = version;
    }

    public void add(String scope, String repeatKey, String... newValues) {
        DataPoint dataPoint = getOrCreateDataPoint(scope, repeatKey);
        for (String value : newValues) {
            dataPoint.add(value);
        }
    }

    public void remove(String scope, String repeatKey, String... newValues) {
        DataPoint dataPoint = getOrCreateDataPoint(scope, repeatKey);
        for (String value : newValues) {
            dataPoint.getValues().remove(value);
        }
    }

    private DataPoint getOrCreateDataPoint(String scope, String repeatKey) {
        Map<String, DataPoint> valuesMap = getOrCreateDataPointMap(scope);
        DataPoint dataPoint =valuesMap.get(repeatKey);
        if (dataPoint==null)
        {
            dataPoint=new DataPoint(repeatKey);
            valuesMap.put(repeatKey, dataPoint);
        }
        return dataPoint;
    }

    private Map<String, DataPoint> getOrCreateDataPointMap(String scope) {
        Map<String, DataPoint> valuesMap = dataPoints.get(scope);
        if (valuesMap == null) {
            valuesMap = new TreeMap<>();
            dataPoints.put(scope, valuesMap);
        }
        return valuesMap;
    }

    public Map<String, Map<String, DataPoint>> getDataPoints() {
        return dataPoints;
    }

    @Override
    public String toString() {
        return "version=" + version +
                ", dataPoints=" + dataPoints.values();
    }

    public DataSliceState nextVersion() {
        DataSliceState nextState = new DataSliceState(version + 1);
        for (Map.Entry<String,  Map<String, DataPoint>> entry : dataPoints.entrySet()) {
            String scope = entry.getKey();
            Map<String, DataPoint> dataPoints = entry.getValue();
            Map<String, DataPoint> nextDataPoints = nextState.getOrCreateDataPointMap(scope);
            for (DataPoint dataPoint : dataPoints.values()) {
                nextDataPoints.put(dataPoint.getRepeatKey(), dataPoint);
            }
        }
        return nextState;
    }

    public int getVersion() {
        return version;
    }


    public void addAfter(String scope, String repeatKey, String after, String ... newValues) {
        DataPoint dataPoint = getOrCreateDataPoint(scope, repeatKey);
        dataPoint.addAfter(after, newValues);
    }
}
