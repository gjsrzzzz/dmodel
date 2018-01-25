package com.jalindi.myweb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.*;

public @Data class DataModel {
    private final List<Event> events=new ArrayList<>();
    private final Map<String, DataPoint> dataPoints=new LinkedHashMap<>();

    public DataModel(int version) {
        events.add(new Event(version));
    }

    private Event firstEvent()
    {
        return events.get(0);
    }

    private void add(String string, String repeatKey) {
        if (dataPoints.containsKey(repeatKey))
        {
            throw new ModelException("Duplicate repeat key "+ repeatKey);
        }
        dataPoints.put(repeatKey, new DataPoint(string, repeatKey, firstEvent(), Event.INFINITY));
    }

    public Event getLastEvent() {
        int version=0;
        Event lastEvent=null;
        for (Event event : events) {
            if (event.getVersion()>=version && event.getVersion()!=Event.INFINITY.getVersion())
            {
                version=event.getVersion();
                lastEvent=event;
            }
        }
        return lastEvent;
    }

    public DataModel nextVersion() {
        Event lastEvent=getLastEvent();
        DataModel nextModel=new DataModel(getLastEvent().getVersion()+1);
        for (DataPoint dataPoint : dataPoints.values())
        {
            if (dataPoint.coversVersion(lastEvent.getVersion())) {
                nextModel.add(dataPoint.getValue(), dataPoint.getRepeatKey());
            }
        }
        return nextModel;
    }

    public void merge(DataModel nextModel) {
        for (Event event : nextModel.events)
        {
            events.add(event);
        }
        int thisVersion=nextModel.firstEvent().getVersion();
        BackLinks links=new BackLinks(dataPoints.values(), nextModel.dataPoints.values(),thisVersion );
        links.log();
        Collection<BackLink> finalLinks=links.process();
        rebuild(finalLinks);
        links.log();
    }

    private void rebuild(Collection<BackLink> finalLinks) {
        Map<Integer, Event> eventMap=new HashMap<>();
        for (Event event : events)
        {
            eventMap.put(event.getVersion(), event);
        }
        eventMap.put(Event.INFINITY.getVersion(), Event.INFINITY);
        dataPoints.clear();
        for (BackLink link : finalLinks) {
            DataPoint dataPoint=new DataPoint(link.getValue(), RepeatSequenceHelper.toRepeatKey(link.getHierarchy()),
                    eventMap.get(link.getValidFrom()), eventMap.get(link.getValidTo()));
            dataPoints.put(dataPoint.getRepeatKey(), dataPoint);
        }
    }

    public void resequence()
    {
        Collection<DataPoint> points=new ArrayList<>(dataPoints.values());
        dataPoints.clear();
        int repeatIndex=RepeatSequenceHelper.nextRepeat(dataPoints.keySet());
        for (DataPoint dataPoint : points)
        {
            add (dataPoint.getValue(), "/"+repeatIndex);
            repeatIndex++;
        }
    }

    public void add(String ... strings ) {
        int repeatIndex=RepeatSequenceHelper.nextRepeat(dataPoints.keySet());
        repeatIndex = addValues(repeatIndex, strings);
    }

    public void addAfter(String afterValue, String... values) {
        Collection<DataPoint> points = new ArrayList<>(dataPoints.values());
        dataPoints.clear();
        int repeatIndex = RepeatSequenceHelper.nextRepeat(dataPoints.keySet());
        boolean first = true;
        for (DataPoint dataPoint : points) {
            if (first && afterValue == null) {
                repeatIndex = addValues(repeatIndex, values);
            }
            add(dataPoint.getValue(), "/" + repeatIndex);
            repeatIndex++;
            if (dataPoint.getValue().equals(afterValue)) {
                repeatIndex = addValues(repeatIndex, values);
            }

            first = false;
        }
    }

    private int addValues(int repeatIndex, String[] values) {
        for (String value : values) {
            add(value, "/" + repeatIndex);
            repeatIndex++;
        }
        return repeatIndex;
    }

    public void remove(String ... strings) {
        String repeatKey=null;
        for (String string : strings) {
            for (Map.Entry<String, DataPoint> entry : dataPoints.entrySet()) {
                if (entry.getValue().getValue().equals(string))
                {
                    repeatKey=entry.getKey();
                }
            }
            dataPoints.remove(repeatKey);
        }
    }

    ModelGrid asGrid() {
        ModelGrid grid = new ModelGrid(this);
       return grid;
    }


}
