package com.jalindi.myweb;

import lombok.extern.java.Log;

import java.util.*;

@Log
public class ModelGrid {
    private final String scope;
    private final int scopeOffset;
    private final String[][] grid;
    private final int[] maxColumnWidths;
    private static final String FIRST_COLUMN="repeatKey";
    private static final String DITTO="--";

    public ModelGrid(DataPointHistory dataPointHistory) {
        this(null, dataPointHistory);
    }
    public ModelGrid(String scope, DataPointHistory dataPointHistory)
    {
        this.scope = scope;
        scopeOffset = scope == null ? 0 : 1;
        Map<Integer, Event> eventMap = new TreeMap<>();
        int numberOfEvents = getNumberOfEvents(dataPointHistory, eventMap);
        Map<String, Integer> repeatRowMap = createRepeatRowMap(dataPointHistory);
        int columns=numberOfEvents + 1 + scopeOffset;
        grid = new String[repeatRowMap.size()][columns];
        maxColumnWidths = new int[columns];
        if (scope!=null)
        {
            maxColumnWidths[0] = scope.length();
        }
        maxColumnWidths[scopeOffset] = FIRST_COLUMN.length();
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
                    String lastValue=columnSearch == 0?"__":grid[row][columnSearch+scopeOffset];
                    while (columnSearch>1 && lastValue.equals(DITTO))
                    {
                        columnSearch--;
                        lastValue=grid[row][columnSearch+scopeOffset];
                    }
                    if (column == 1 || !lastValue.equals(dataPointValue.getValue())) {
                        cell = dataPointValue.getValue();
                    } else {
                        cell = DITTO;
                    }
                }
                if (cell.length()>0 || grid[row][column+scopeOffset]==null) {
                    grid[row][column+scopeOffset] = cell;
                }
                maxColumnWidths[column+scopeOffset] = Math.max(cell.length(), maxColumnWidths[column+scopeOffset]);
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
        int row = 0;
        for (String repeatKey : repeatRowMap.keySet()) {
            if (scope != null) {
                grid[row][0] = scope;
            }
            grid[row++][scopeOffset] = repeatKey;
            maxColumnWidths[scopeOffset] = Math.max(repeatKey.length(), maxColumnWidths[0]);
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

    public String toStringWithoutHeader()
    {
        StringBuilder builder = new StringBuilder();
        addGridData(builder);
        return builder.toString();
    }

    private void addGridData(StringBuilder builder) {
        for (int r = 0; r < grid.length; r++) {
            if (r>0)
            {
                builder.append("\n");
            }
            for (int c = 0; c < maxColumnWidths.length; c++) {
                int size = maxColumnWidths[c];
                builder.append(String.format("%"+size+"s ", grid[r][c]));
            }
        }
    }

    private void addColumnHeaders(StringBuilder builder) {
        for (int c = 0; c < maxColumnWidths.length; c++) {
            String headerText=c == scopeOffset ? FIRST_COLUMN : Integer.toString(c - 1- scopeOffset);
            if (scope!=null && c==0)
            {
                headerText="scope";
            }
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

    public void mergeColumnWidths(ModelGrid otherGrid) {
        for (int r = 0; r < grid.length; r++) {
             for (int c = 0; c < maxColumnWidths.length; c++) {
                 maxColumnWidths[c] = Math.max(maxColumnWidths[c], otherGrid.maxColumnWidths[c]);
            }
        }
    }
}
