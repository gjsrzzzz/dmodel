package com.jalindi.myweb;

import lombok.Data;

import java.util.*;

public @Data class DataModel {
    private final List<Event> events=new ArrayList<>();
    private final Map<RepeatCoverage, DataPoint> dataPoints=new LinkedHashMap<>();

    public DataModel() {
        events.add(new Event(0));
    }

    private DataModel(int version) {
        events.add(new Event(version));
    }

    private Event firstEvent()
    {
        return events.get(0);
    }

    private void add(String string, String repeatKey) {
        if (dataPoints.containsKey(RepeatCoverage.create(events, repeatKey)))
        {
            throw new ModelException("Duplicate repeat key "+ repeatKey);
        }
        addDataPoint(new DataPoint(string, repeatKey, firstEvent(), Event.INFINITY));
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
            String repeatKey=link.getRepeatSequence().getRepeatKey();
            addLink(eventMap, repeatKey, link);
            for (BackLink mergedLink: link.getMergedWith())
            {
                addLink(eventMap, repeatKey, mergedLink);
            }
        }
    }

    private void addLink(Map<Integer, Event> eventMap, String repeatKey, BackLink link) {
        boolean[] coverage= link.getCoverageWithoutMerged();
        int validFrom=-1;
        boolean on=false;
        for (int version=0; version<coverage.length;version++)
        {
            boolean covers=coverage[version];
            if (on)
            {
                if (!covers)
                {
                    DataPoint dataPoint=new DataPoint(link.getValue(), repeatKey,
                            eventMap.get(validFrom), eventMap.get(version-1));
                    addDataPoint(dataPoint);
                    on=false;
                }
            }
            else
            {
                if (covers)
                {
                    on=true;
                    validFrom=version;
                }
            }
        }
        if (on) {
            DataPoint dataPoint = new DataPoint(link.getValue(), repeatKey,
                    eventMap.get(validFrom), eventMap.get(link.getValidTo()));
            addDataPoint(dataPoint);
        }
    }

    private void addDataPoint(DataPoint dataPoint) {
        dataPoints.put(dataPoint.getRepeatCoverage(), dataPoint);
    }

    /* Resequence the repeats starting at 1
    note: currently this will also set the version range for each datapoint to infinity
    todo: this will not work if this is nested
     */
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
        repeatIndex = addValues(repeatIndex, "/", strings);
    }

    public void addAtRepeat(String repeatPrefix, String ... strings ) {
        if (!repeatPrefix.endsWith("/"))
        {
            repeatPrefix=repeatPrefix+"/";
        }
        int repeatIndex=RepeatSequenceHelper.nextRepeat(repeatPrefix, dataPoints.keySet());
        repeatIndex = addValues(repeatIndex, repeatPrefix, strings);
    }

    /* inserts values into the list after a certain data point
       note: all values are reinserted to this also resequences
     */
    public void addAfter(String afterValue, String... values) {
        Collection<DataPoint> points = new ArrayList<>(dataPoints.values());
        dataPoints.clear();
        int repeatIndex = RepeatSequenceHelper.nextRepeat(dataPoints.keySet());
        boolean first = true;
        for (DataPoint dataPoint : points) {
            if (first && afterValue == null) {
                repeatIndex = addValues(repeatIndex, "/", values);
            }
            add(dataPoint.getValue(), "/" + repeatIndex);
            repeatIndex++;
            if (dataPoint.getValue().equals(afterValue)) {
                repeatIndex = addValues(repeatIndex, "/", values);
            }

            first = false;
        }
    }

    private int addValues(int repeatIndex, final String repeatPrefix, final String[] values) {
        if (!repeatPrefix.endsWith("/"))
        {
            throw new ModelException("repeat prefix must end with /");
        }
        for (String value : values) {
            add(value, repeatPrefix + repeatIndex);
            repeatIndex++;
        }
        return repeatIndex;
    }

    public void remove(String ... strings) {
        RepeatCoverage repeatKey=null;
        for (String string : strings) {
            for (Map.Entry<RepeatCoverage, DataPoint> entry : dataPoints.entrySet()) {
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
