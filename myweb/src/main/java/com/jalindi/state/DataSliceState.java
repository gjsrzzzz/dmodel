package com.jalindi.state;

import com.jalindi.myweb.DataState;

import java.util.*;

public class DataSliceState {
    private final int version;
    private final Map<String, Map<String, Container>> containers = new LinkedHashMap<>();
    private final Map<String, Map<String, DataPoint>> dataPoints = new LinkedHashMap<>();

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

    public void addContainer(String scope, String repeatKey) {
        Container container = getOrCreateContainer(scope, repeatKey);
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

    private Container getOrCreateContainer(String scope, String repeatKey) {
        Map<String, Container> containersMap = getOrCreateContainerMap(scope);
        Container container =containersMap.get(repeatKey);
        if (container==null)
        {
            container=new Container(repeatKey);
            containersMap.put(repeatKey, container);
        }
        return container;
    }

    private Map<String, Container> getOrCreateContainerMap(String scope) {
        Map<String, Container> containersMap = containers.get(scope);
        if (containersMap == null) {
            containersMap = new TreeMap<>();
            containers.put(scope, containersMap);
        }
        return containersMap;
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
        for (Map.Entry<String,  Map<String, Container>> entry : containers.entrySet()) {
            String scope = entry.getKey();
            Map<String, Container> containers = entry.getValue();
            Map<String, Container> nextDataContainers = nextState.getOrCreateContainerMap(scope);
            for (Container container : containers.values()) {
                nextDataContainers.put(container.getRepeatKey(), container);
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

    public Map<String, Map<String, Container>> getContainers() {
        return containers;
    }

    public void addContainerAt(String scope, String repeatPrefix, int index) {
        Map<String, Container> containersMap = getOrCreateContainerMap(scope);
        int i=0;
        Container previousContainer=null;
        ArrayList<Container> newList=new ArrayList();
        for (Container container : containersMap.values())
        {
            if (index==i)
            {
                newList.add(new Container(repeatPrefix+(index+1)));
            }
            newList.add(container);
            previousContainer=container;
            i++;
        }
        containersMap.clear();
        for (Container container : newList)
        {
            containersMap.put(container.getRepeatKey(), container);
        }
    }
}
