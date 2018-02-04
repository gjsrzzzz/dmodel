package com.jalindi.myweb;

import com.jalindi.state.DataPoint;
import com.jalindi.state.DataSliceState;
import lombok.extern.java.Log;

import java.util.*;

@Log
public class DataState {
    private final List<Event> events = new ArrayList<>();
    private final Map<String, List<DataPointValue>> dataPoints = new LinkedHashMap<>();

    public void setDataSlice(DataSliceState dataSlice) {
  //      if (true || dataSlice.getVersion() >= getHighestVersion()) {
            addDataSliceInternal(dataSlice);
  //          return;
  //      }
      /* Not needed, fixed slicer DataSliceState oldSlice = getDataSlice(dataSlice.getVersion() + 1);

       Event event=getOrCreateEvent(dataSlice);
        DataState sliceState=new DataState();
        sliceState.addDataSliceInternal(dataSlice);
        sliceState.addDataSliceInternal(oldSlice);
        ModelGrid grid=sliceState.asGrid("C.A");
        log.info("Model\n"+grid);
        for (Map.Entry<String, List<DataPointValue>> entry : sliceState.dataPoints.entrySet()) {
            String scope = entry.getKey();
            List<DataPointValue> values=entry.getValue();
            copyBackToModel(event, scope, values);
        }*/
    }

   /* private void copyBackToModel(Event sliceEvent, String scope, List<DataPointValue> values) {
        DataPointHistory model = createHistoryModel(scope, null);
        model.log();
        model.slice(sliceEvent);
        model.log();

        for (DataPointValue value : values) {
            if (value.coversVersion(sliceEvent.getVersion())) {
                model.addDataPoint(value);
            }
        }
        model.merge(sliceEvent);
        model.log();
        copyModelBackToState(scope, model);
    }*/

    private void addDataSliceInternal(DataSliceState dataSlice) {
        Event event = getOrCreateEvent(dataSlice);
        for (Map.Entry<String, Map<String, DataPoint>> entry : dataSlice.getDataPoints().entrySet()) {
            String scope = entry.getKey();
            Map<String, DataPoint> dataPoints = entry.getValue();
            addDataPoint(event, scope, dataPoints.values(), event.getVersion() == getHighestVersion() ? Event.INFINITY : event);
        }
    }

    public DataSliceState getDataSlice(int version) {
        Event event = getEvent(version);
        DataSliceState state = new DataSliceState(version);
        for (Map.Entry<String, List<DataPointValue>> entry : dataPoints.entrySet()) {
            String scope = entry.getKey();
            for (DataPointValue value : entry.getValue()) {
                if (value.coversVersion(version)) {
                    RepeatSequenceHelper.RepeatSequence repeatSequence = RepeatSequenceHelper.createRepeatSequence(value.getRepeatKey());
                    String repeatKey = repeatSequence.getRepeatKeyForLastInHierarchy();
                    state.add(scope, repeatKey, value.getValue());
                }
            }
        }
        return state;
    }

    private Event getEvent(int version) {
        Event event = null;
        for (Event scanEvent : events) {
            if (scanEvent.getVersion() == version) {
                event = scanEvent;
            }
        }
        return event;
    }

    private int getHighestVersion() {
        int highestVersion = 0;
        for (Event scanEvent : events) {
            highestVersion = Math.max(highestVersion, scanEvent.getVersion());
        }
        return highestVersion;
    }

    private Event getOrCreateEvent(DataSliceState dataSlice) {
        Event event = getEvent(dataSlice.getVersion());
        if (event == null) {
            event = new Event(dataSlice.getVersion());
            events.add(event);
        }
        return event;
    }

    private void addDataPoint(Event sliceEvent, String scope, Collection<DataPoint> dataPoints, Event endEvent) {
        DataPointHistory model = createHistoryModel(scope, null);
        model.log();
        model.slice(sliceEvent);
        model.log();
        for (DataPoint dataPoint : dataPoints) {
            //   dataPoint.getRepeatKey();
            int index = 1;
            for (String value : dataPoint.getValues()) {
                //        model.add(value);
                model.addDataPoint(new DataPointValue(value, dataPoint.getRepeatKey() + "/" + index,
                        sliceEvent, endEvent));
                index++;
            }
        }
        model.merge();
        copyModelBackToState(scope, model);
    }

   /*     private void addDataPoint2(Event sliceEvent, String scope, List<DataPoint> dataPoints) {
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
    }*/

    private void copyModelBackToState(String scope, DataPointHistory model) {
        List<DataPointValue> dataPointValues = getOrCreateDataPointValue(scope);
        dataPointValues.clear();
        for (DataPointValue dataPoint : model.getDataPoints().values()) {
            dataPointValues.add(dataPoint);
        }
    }

    private DataPointHistory createHistoryModel(String scope, Event sliceEvent) {
        DataPointHistory model = null;
        Event previousEvent = null;
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
        List<DataPointValue> dataPointValues = getOrCreateDataPointValue(scope);
        for (DataPointValue dataPoint : dataPointValues) {
            if (previousEvent != null && dataPoint.getValidTo().equals(sliceEvent)) {
                dataPoint = new DataPointValue(dataPoint.getValue(), dataPoint.getRepeatKey(),
                        dataPoint.getValidFrom(), previousEvent);
            }
            model.addDataPoint(dataPoint);
        }
        return model;
    }

    private List<DataPointValue> getOrCreateDataPointValue(String scope) {
        List<DataPointValue> dataPointValues = dataPoints.get(scope);
        if (dataPointValues == null) {
            dataPointValues = new ArrayList<>();
            dataPoints.put(scope, dataPointValues);
        }
        return dataPointValues;
    }

    public String[][] asGrid()
    {
        List<ModelGrid> grids=new ArrayList<>();
        ModelGrid firstGrid = createModelGrids(grids);
        int rows=0;
        for (ModelGrid grid : grids)
        {
            rows+=grid.getGrid().length;
        }
        String result[][]=new String[rows][firstGrid.getGrid()[0].length];
        int row=0;
        for (ModelGrid grid : grids)
        {
            for (int r = 0; r < grid.getGrid().length; r++) {
                for (int c = 0; c < grid.getGrid()[0].length; c++) {
                    result[row][c]=grid.getGrid()[r][c];
                }
                row++;
            }
        }
        return result;
    }

    public ModelGrid asGrid(String scope) {
        return asGrid(scope, false);

   }


    public ModelGrid asGrid(String scope, boolean showScope) {
        DataPointHistory model = createHistoryModel(scope, null);
        ModelGrid grid = model.asGrid(showScope ? scope : null);
        return grid;
    }

    public void log()
    {
        StringBuilder builder=new StringBuilder();
        List<ModelGrid> grids=new ArrayList<>();
        ModelGrid firstGrid = createModelGrids(grids);
        boolean first=true;
        for (ModelGrid grid : grids)
        {
            grid.mergeColumnWidths(firstGrid);
            if (!first)
            {
                builder.append("\n");
            }
            String scopeGrid=first?grid.toString():grid.toStringWithoutHeader();
            builder.append(scopeGrid);
            first=false;
        }
        log.info("Model\n"+builder.toString());
    }

    private ModelGrid createModelGrids(List<ModelGrid> grids) {
        ModelGrid firstGrid=null;
        for (String scope : dataPoints.keySet())
        {
            ModelGrid grid=asGrid(scope, true);
            grids.add(grid);
            if (firstGrid==null)
            {
                firstGrid=grid;
            }
            else
            {
                firstGrid.mergeColumnWidths(grid);
            }
        }
        return firstGrid;
    }
}
