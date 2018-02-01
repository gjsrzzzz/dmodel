package com.jalindi.myweb;

import com.jalindi.state.DataPoint;
import com.jalindi.state.DataSliceState;
import lombok.extern.java.Log;

import java.util.*;

@Log
public class DataState {
    private final List<Event> events=new ArrayList<>();
    private final Map<String, List<DataPointValue>> dataPoints=new HashMap<>();

    public void setDataSlice(DataSliceState dataSlice) {
        Event event=getOrCreateEvent(dataSlice);
        for (Map.Entry<String, Map<String, DataPoint>> entry : dataSlice.getDataPoints().entrySet())
        {
            String scope=entry.getKey();
            Map<String, DataPoint> dataPoints=entry.getValue();
            addDataPoint(event, scope, dataPoints.values());
        }
    }


    public DataSliceState getDataSlice(int version) {
        Event event= getEvent(version);
        DataSliceState state=new DataSliceState(version);
        for (Map.Entry<String, List<DataPointValue>> entry : dataPoints.entrySet())
        {
            String scope=entry.getKey();
            for (DataPointValue value : entry.getValue())
            {
                if (value.coversVersion(version))
                {
                    RepeatSequenceHelper.RepeatSequence repeatSequence=RepeatSequenceHelper.createRepeatSequence(value.getRepeatKey());
                    String repeatKey = repeatSequence.getRepeatKeyForLastInHierarchy();
                    state.add(scope, repeatKey,value.getValue());
                }
            }
        }
        return state;
    }

    private Event getEvent(int version) {
        Event event=null;
        for (Event scanEvent : events)
        {
            if (scanEvent.getVersion()==version)
            {
                event=scanEvent;
            }
        }
        return event;
    }

    private Event getOrCreateEvent(DataSliceState dataSlice) {
        Event event=getEvent(dataSlice.getVersion());
        if (event==null)
        {
            event=new Event(dataSlice.getVersion());
            events.add(event);
        }
       return event;
    }

    private void addDataPoint(Event sliceEvent, String scope, Collection<DataPoint> dataPoints) {
        DataPointHistory model = createHistoryModel(scope, null);
        model.log();
        model.slice(sliceEvent);
        model.log();
        for (DataPoint dataPoint : dataPoints)
        {
            //   dataPoint.getRepeatKey();
            int index=1;
            for (String value : dataPoint.getValues()) {
        //        model.add(value);
                model.addDataPoint(new DataPointValue(value, dataPoint.getRepeatKey()+"/"+index,
                        sliceEvent, Event.INFINITY));
                index++;
            }
        }
        model.merge(sliceEvent);
        copyModelBackToState(scope, model);
    }

        private void addDataPoint2(Event sliceEvent, String scope, List<DataPoint> dataPoints) {
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
            copyModelBackToState(scope, model);
    }

    private void copyModelBackToState(String scope, DataPointHistory model) {
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
