package com.jalindi.myweb;

import com.jalindi.state.DataPoint;
import com.jalindi.state.DataSliceState;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class DataState {
    private final List<Event> events=new ArrayList<>();
    private final Map<String, List<DataPointValue>> dataPoints=new HashMap<>();

    public void setDataSlice(DataSliceState dataSlice) {
        Event event=getOrCreateEvent(dataSlice);
        for (Map.Entry<String, List<DataPoint>> entry : dataSlice.getDataPoints().entrySet())
        {
            String scope=entry.getKey();
            List<DataPoint> dataPoints=entry.getValue();
            addDataPoint(event, scope, dataPoints);
        }
    }

    private Event getOrCreateEvent(DataSliceState dataSlice) {
        Event event=null;
        for (Event scanEvent : events)
        {
            if (scanEvent.getVersion()==dataSlice.getVersion())
            {
                event=scanEvent;
            }
        }
        if (event==null)
        {
            event=new Event(dataSlice.getVersion());
            events.add(event);
        }
       return event;
    }

    private void addDataPoint(Event sliceEvent, String scope, List<DataPoint> dataPoints) {
        DataPointHistory model=null;
        DataPointHistory sliceHistory=new DataPointHistory(sliceEvent);
        for (DataPoint dataPoint : dataPoints)
        {
         //   dataPoint.getRepeatKey();
            for (String value : dataPoint.getValues()) {
                sliceHistory.add(value);
            }
        }
        if (sliceEvent.getVersion()==0)
        {
            model=sliceHistory;
        }
        else {
            model=createHistoryModel(scope, sliceEvent);
            model.merge(sliceHistory);
        }
        List<DataPointValue> dataPointValues=getOrCreateDataPointValue(scope);
        dataPointValues.clear();
        for (DataPointValue dataPoint : model.getDataPoints().values())
        {
            dataPointValues.add(dataPoint);
        }
    }

    private DataPointHistory createHistoryModel(String scope, Event sliceEvent)
    {
        DataPointHistory model=null;
        Event previousEvent=null;
        for (Event event : events) {
            if (sliceEvent != null && event.getVersion() == sliceEvent.getVersion() - 1) {
                previousEvent = event;
            }
            if (sliceEvent == null || event.getVersion() != sliceEvent.getVersion()) {
                if (model == null) {
                    model = new DataPointHistory(event);
                } else {
                    model.getEvents().add(event);
                }
            }

        }
        List<DataPointValue> dataPointValues=getOrCreateDataPointValue(scope);
        for (DataPointValue dataPoint : dataPointValues)
        {
            if (previousEvent!=null && dataPoint.getValidTo().equals(sliceEvent))
            {
                dataPoint=new DataPointValue(dataPoint.getValue(), dataPoint.getRepeatKey(),
                        dataPoint.getValidFrom(), previousEvent);
            }
            model.addDataPoint(dataPoint);
        }
        return model;
    }

    private List<DataPointValue> getOrCreateDataPointValue(String scope) {
        List<DataPointValue> dataPointValues=dataPoints.get(scope);
        if (dataPointValues==null)
        {
            dataPointValues=new ArrayList<>();
            dataPoints.put(scope, dataPointValues);
        }
        return dataPointValues;
    }

    public ModelGrid asGrid(String scope) {
        DataPointHistory model=createHistoryModel(scope, null);
        ModelGrid grid=model.asGrid();
        return grid;
    }
}
