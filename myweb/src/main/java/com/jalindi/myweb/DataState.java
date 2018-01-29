package com.jalindi.myweb;

import com.jalindi.state.DataPoint;
import com.jalindi.state.DataSliceState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void addDataPoint(Event event, String scope, List<DataPoint> dataPoints) {
        DataPointHistory model=new DataPointHistory();
        for (DataPoint dataPoint : dataPoints)
        {
         //   dataPoint.getRepeatKey();
            for (String value : dataPoint.getValues()) {
                model.add(value);
            }
        }
        List<DataPointValue> dataPointValues=getOrCreateDataPointValue(scope);
        for (DataPointValue dataPoint : model.getDataPoints().values())
        {
            dataPointValues.add(dataPoint);
        }
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
        DataPointHistory model=new DataPointHistory();
        List<DataPointValue> values=getOrCreateDataPointValue(scope);
        for (DataPointValue dataPoint : values)
        {
            model.addDataPoint(dataPoint);
        }
        ModelGrid grid=model.asGrid();
        return grid;
    }
}
