package com.jalindi.myweb;

import lombok.Data;
import lombok.extern.java.Log;

import java.util.*;

@Log
public @Data class DataPointHistory {
    private final List<Event> events=new ArrayList<>();
    private final Map<RepeatCoverage, DataPointValue> dataPoints=new LinkedHashMap<>();

    public DataPointHistory() {
        events.add(new Event(0));
    }

    private DataPointHistory(int version) {
        events.add(new Event(version));
    }

    public DataPointHistory(Event event) {
        events.add(event);
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
        addDataPoint(new DataPointValue(string, repeatKey, firstEvent(), Event.INFINITY));
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

    public DataPointHistory nextVersion() {
        Event lastEvent=getLastEvent();
        DataPointHistory nextModel=new DataPointHistory(getLastEvent().getVersion()+1);
        for (DataPointValue dataPointValue : dataPoints.values())
        {
            if (dataPointValue.coversVersion(lastEvent.getVersion())) {
                nextModel.add(dataPointValue.getValue(), dataPointValue.getRepeatKey());
            }
        }
        return nextModel;
    }

/*    public void merge(DataPointHistory nextModel) {
        for (Event event : nextModel.events)
        {
            events.add(event);
        }
        int thisVersion=nextModel.firstEvent().getVersion();
        BackLinks links=new BackLinks(dataPoints.values(), nextModel.dataPoints.values(),thisVersion );
        processBackLinkScanResequence(links);
    }*/

    private ResequencedItems processBackLinkScanResequence(BackLinks links) {
        links.log();
        ResequencedItems resequencedItems = links.process();
        Collection<BackLink> finalLinks=links.getNextLinks();
        rebuild(finalLinks);
        links.log();
        return resequencedItems;
    }

    public ResequencedItems merge(DataPointHistory nextModel) {
        for (Event event : nextModel.events)
        {
            events.add(event);
        }
        slice(nextModel.getLastEvent());
        for (Map.Entry<RepeatCoverage,DataPointValue> entry : nextModel.dataPoints.entrySet())
        {
            dataPoints.put(entry.getKey(), entry.getValue());
        }
        return merge();
    }

    public ResequencedItems merge() {
        Event lastEvent=getLastEvent();
        BackLinks links=new BackLinks(dataPoints.values(), lastEvent);
        ResequencedItems resequencedItems=processBackLinkScanResequence(links);
        return resequencedItems;
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
                    DataPointValue dataPointValue =new DataPointValue(link.getValue(), repeatKey,
                            eventMap.get(validFrom), eventMap.get(version-1));
                    addDataPoint(dataPointValue);
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
            DataPointValue dataPointValue = new DataPointValue(link.getValue(), repeatKey,
                    eventMap.get(validFrom), eventMap.get(link.getValidTo()));
            addDataPoint(dataPointValue);
        }
    }

    void addDataPoint(DataPointValue dataPointValue) {
        dataPoints.put(dataPointValue.getRepeatCoverage(), dataPointValue);
    }

    /* Resequence the repeats starting at 1
    note: currently this will also set the version range for each datapoint to infinity
     */
    public void resequence()
    {
        addAfter(null, null);
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
        Resequencer resequencer=new Resequencer(dataPoints.values(), 0);
        resequencer.resequence(getLastEvent().getVersion(), afterValue, values);
        rebuild(resequencer.getLinks());
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
            for (Map.Entry<RepeatCoverage, DataPointValue> entry : dataPoints.entrySet()) {
                if (entry.getValue().getValue().equals(string))
                {
                    repeatKey=entry.getKey();
                }
            }
            dataPoints.remove(repeatKey);
        }
    }

    ModelGrid asGrid(String scope) {
        ModelGrid grid = new ModelGrid(scope,this);
       return grid;
    }

    ModelGrid asGrid() {
        ModelGrid grid = new ModelGrid(this);
        return grid;
    }

    public int size() {
        return dataPoints.size();
    }

    public void slice(Event sliceEvent) {
        int sliceVersion=sliceEvent.getVersion();
        Event eventBeforeSlice=null;
        Event eventAfterSlice=null;
        for (Event event : events)
        {
            if (event.getVersion()==sliceVersion-1)
            {
                eventBeforeSlice=event;
            }
            if (event.getVersion()==sliceVersion+1)
            {
                eventAfterSlice=event;
            }
        }
        List<DataPointValue> copy=new ArrayList<>(dataPoints.values());
        dataPoints.clear();
        for (DataPointValue dataPointValue : copy) {
            if (dataPointValue.getValidFrom().getVersion() < sliceVersion) {
                if (dataPointValue.getValidTo().getVersion() >= sliceVersion) {
                    addDataPoint(new DataPointValue(dataPointValue.getValue(), dataPointValue.getRepeatKey(),
                            dataPointValue.getValidFrom(), eventBeforeSlice));
                    if (eventAfterSlice!=null && dataPointValue.getValidTo().getVersion()>=eventAfterSlice.getVersion())
                    {
                        addDataPoint(new DataPointValue(dataPointValue.getValue(), dataPointValue.getRepeatKey(),
                                eventAfterSlice, dataPointValue.getValidTo()));
                    }
                } else {
                    addDataPoint(dataPointValue);
                }
            } else if (dataPointValue.getValidFrom().getVersion() > sliceVersion) {
                addDataPoint(dataPointValue);
            }
            else
            {
                if (eventAfterSlice!=null && dataPointValue.getValidTo().getVersion()>=eventAfterSlice.getVersion())
                {
                    addDataPoint(new DataPointValue(dataPointValue.getValue(), dataPointValue.getRepeatKey(),
                            eventAfterSlice, dataPointValue.getValidTo()));
                }
            }
        }
    }

    public void log() {
        log.info("Model\n"+asGrid(null));
    }
}
