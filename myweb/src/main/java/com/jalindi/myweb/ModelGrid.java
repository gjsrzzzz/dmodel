package com.jalindi.myweb;

import lombok.extern.java.Log;

import java.util.*;

@Log
public class ModelGrid {
    private final String[][] grid;
    private final int[] maxColumnWidths;
    private static final String FIRST_COLUMN="repeatKey";
    private static final String DITTO="--";

    public ModelGrid(DataPointHistory dataPointHistory) {
        Map<Integer, Event> eventMap = new TreeMap<>();
        int numberOfEvents = getNumberOfEvents(dataPointHistory, eventMap);
        Map<String, Integer> repeatRowMap = createRepeatRowMap(dataPointHistory);
        grid = new String[repeatRowMap.size()][numberOfEvents + 1];
        maxColumnWidths = new int[numberOfEvents + 1];
        maxColumnWidths[0] = FIRST_COLUMN.length();
        createRepeatKeyColumn(repeatRowMap);
        createDataColumns(dataPointHistory, eventMap, repeatRowMap);
    }

    private void createDataColumns(DataPointHistory dataPointHistory, Map<Integer, Event> eventMap, Map<String, Integer> repeatRowMap) {
        int column =1;
        for (Event event : eventMap.values()) {
            for (Map.Entry<RepeatCoverage, DataPointValue> entry: dataPointHistory.getDataPoints().entrySet()) {
                DataPointValue dataPointValue =entry.getValue();
                int row=repeatRowMap.get(entry.getKey().getRepeatKey());
                String cell = "";
                if (dataPointValue.coversVersion(event.getVersion())) {
                    int columnSearch=column - 1;
                    String lastValue=columnSearch == 0?"__":grid[row][columnSearch];
                    while (columnSearch>1 && lastValue.equals(DITTO))
                    {
                        columnSearch--;
                        lastValue=grid[row][columnSearch];
                    }
                    if (column == 1 || !lastValue.equals(dataPointValue.getValue())) {
                        cell = dataPointValue.getValue();
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

    private static Map<String, Integer> createRepeatRowMap(DataPointHistory dataPointHistory) {
        Map<String, Integer> repeats=new LinkedHashMap<>();
        {
            int row = 0;
            for (RepeatCoverage repeatCoverage : dataPointHistory.getDataPoints().keySet()) {
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

    private int getNumberOfEvents(DataPointHistory dataPointHistory, Map<Integer, Event> eventMap) {
        int numberOfEvents = 0;
        for (Event event : dataPointHistory.getEvents()) {
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
            String headerText=c == 0 ? FIRST_COLUMN : Integer.toString(c - 1);
            maxColumnWidths[c] = Math.max(headerText.length(), maxColumnWidths[c]);
            int columnWidth = maxColumnWidths[c];
            if (columnWidth>0) {
                try {
                    builder.append(String.format("%" + columnWidth + "s ", headerText));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log.severe("Failed to create item : '%" + columnWidth + "s '");
                    throw ex;
                }
            }
        }
    }

    public String[][] getGrid() {
        return grid;
    }
}
