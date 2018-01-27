package com.jalindi.myweb;

import lombok.extern.java.Log;

import java.util.*;

@Log
public class ModelGrid {
    private final String[][] grid;
    private final int[] maxColumnWidths;
    private static final String FIRST_COLUMN="repeatKey";
    private static final String DITTO="--";

    public ModelGrid(DataModel dataModel) {
        Map<Integer, Event> eventMap = new TreeMap<>();
        int numberOfEvents = getNumberOfEvents(dataModel, eventMap);
        Map<String, Integer> repeatRowMap = createRepeatRowMap(dataModel);
        grid = new String[repeatRowMap.size()][numberOfEvents + 1];
        maxColumnWidths = new int[numberOfEvents + 1];
        maxColumnWidths[0] = FIRST_COLUMN.length();
        createRepeatKeyColumn(repeatRowMap);
        createDataColumns(dataModel, eventMap, repeatRowMap);
    }

    private void createDataColumns(DataModel dataModel, Map<Integer, Event> eventMap, Map<String, Integer> repeatRowMap) {
        int column =1;
        for (Event event : eventMap.values()) {
            for (Map.Entry<RepeatCoverage, DataPoint> entry: dataModel.getDataPoints().entrySet()) {
                DataPoint dataPoint=entry.getValue();
                int row=repeatRowMap.get(entry.getKey().getRepeatKey());
                String cell = "";
                if (dataPoint.coversVersion(event.getVersion())) {
                    int columnSearch=column - 1;
                    String lastValue=columnSearch == 0?"__":grid[row][columnSearch];
                    while (columnSearch>1 && lastValue.equals(DITTO))
                    {
                        columnSearch--;
                        lastValue=grid[row][columnSearch];
                    }
                    if (column == 1 || !lastValue.equals(dataPoint.getValue())) {
                        cell = dataPoint.getValue();
                    } else {
                        cell = DITTO;
                    }
                }
                if (cell.length()>0 || grid[row][column]==null) {
                    grid[row][column] = cell;
                }
                maxColumnWidths[column] = Math.max(cell.length(), maxColumnWidths[column]);
            }
            column++;
        }
    }

    private static Map<String, Integer> createRepeatRowMap(DataModel dataModel) {
        Map<String, Integer> repeats=new LinkedHashMap<>();
        {
            int row = 0;
            for (RepeatCoverage repeatCoverage : dataModel.getDataPoints().keySet()) {
                if (!repeats.containsKey(repeatCoverage.getRepeatKey())) {
                    repeats.put(repeatCoverage.getRepeatKey(), row++);
                }
            }
        }
        return repeats;
    }

    private void createRepeatKeyColumn(Map<String, Integer> repeatRowMap) {
        int row=0;
        for (String repeatKey : repeatRowMap.keySet()) {
                grid[row++][0] = repeatKey;
                maxColumnWidths[0] = Math.max(repeatKey.length(), maxColumnWidths[0]);
        }
    }

    private int getNumberOfEvents(DataModel dataModel, Map<Integer, Event> eventMap) {
        int numberOfEvents = 0;
        for (Event event : dataModel.getEvents()) {
            if (!event.equals(Event.INFINITY)) {
                eventMap.put(event.getVersion(), event);
                numberOfEvents++;
            }
        }
        return numberOfEvents;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        addColumnHeaders(builder);
        builder.append("\n");
        addGridData(builder);
        return builder.toString();
    }

    private void addGridData(StringBuilder builder) {
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < maxColumnWidths.length; c++) {
                int size = maxColumnWidths[c];
                builder.append(String.format("%"+size+"s ", grid[r][c]));
            }
            builder.append("\n");
        }
    }

    private void addColumnHeaders(StringBuilder builder) {
        for (int c = 0; c < maxColumnWidths.length; c++) {
            int columnWidth = maxColumnWidths[c];
            try {
                builder.append(String.format("%" + columnWidth + "s ", (c == 0 ? FIRST_COLUMN : Integer.toString(c-1))));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                log.severe("Failed to create item : '%" + columnWidth + "s '");
                throw ex;
            }
        }
    }

    public String[][] getGrid() {
        return grid;
    }
}
