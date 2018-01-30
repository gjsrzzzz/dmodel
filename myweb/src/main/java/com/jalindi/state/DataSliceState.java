package com.jalindi.state;

import com.jalindi.myweb.DataState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSliceState {
    private final int version;
    private final Map<String, List<DataPoint>> dataPoints = new HashMap<>();

    public DataSliceState() {
        version = 0;
    }

    private DataSliceState(int version) {
        this.version = version;
    }

    public void add(String scope, String... newValues) {
        List<DataPoint> values = getOrCreateDataPoints(scope);
        DataPoint dataPoint = null;
        if (values.size() == 0) {
            dataPoint = new DataPoint();
            values.add(dataPoint);
        } else {
            dataPoint = values.get(0);
        }
        for (String value : newValues) {
            dataPoint.add(value);
        }
    }

    public void remove(String scope, String... newValues) {
        List<DataPoint> values = getOrCreateDataPoints(scope);
        DataPoint dataPoint = null;
        if (values.size() > 0)
            dataPoint = values.get(0);
        for (String value : newValues) {
            dataPoint.getValues().remove(value);
        }
    }
    

    private List<DataPoint> getOrCreateDataPoints(String scope) {
        List<DataPoint> values = dataPoints.get(scope);
        if (values == null) {
            values = new ArrayList<>();
            dataPoints.put(scope, values);
        }

        return values;
    }

    public Map<String, List<DataPoint>> getDataPoints() {
        return dataPoints;
    }

    @Override
    public String toString() {
        return "version=" + version +
                ", dataPoints=" + dataPoints.values();
    }

    public DataSliceState nextVersion() {
        DataSliceState nextState = new DataSliceState(version + 1);
        for (Map.Entry<String, List<DataPoint>> entry : dataPoints.entrySet()) {
            String scope = entry.getKey();
            List<DataPoint> dataPoints = entry.getValue();
            List<DataPoint> nextDataPoints = nextState.getOrCreateDataPoints(scope);
            for (DataPoint dataPoint : dataPoints) {
                nextDataPoints.add(dataPoint);
            }
        }
        return nextState;
    }

    public int getVersion() {
        return version;
    }


}
