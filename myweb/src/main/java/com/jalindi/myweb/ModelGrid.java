package com.jalindi.myweb;

import lombok.extern.java.Log;

import java.util.Map;
import java.util.TreeMap;

@Log
public class ModelGrid {
    private final String[][] grid;
    private final int[] maxSizes;
    private static final String FIRST_COLUMN="repeatKey";
    private static final String DITTO="--";

    public ModelGrid(DataModel dataModel) {
        Map<Integer, Event> eventMap = new TreeMap<>();
        int numberOfEvents = 0;
        for (Event event : dataModel.getEvents()) {
            if (!event.equals(Event.INFINITY)) {
                eventMap.put(event.getVersion(), event);
                numberOfEvents++;
            }
        }
        grid = new String[dataModel.getDataPoints().size()][numberOfEvents + 1];
        maxSizes = new int[numberOfEvents + 1];
        maxSizes[0] = FIRST_COLUMN.length();
        int index = 0;
        int column = 0;
        for (String repeatKey : dataModel.getDataPoints().keySet()) {
            grid[index++][column] = repeatKey;
            maxSizes[column] = Math.max(repeatKey.length(), maxSizes[column]);
        }
        column++;
        for (Event event : eventMap.values()) {
            index = 0;
            for (DataPoint dataPoint : dataModel.getDataPoints().values()) {
                String cell = "";
                if (dataPoint.coversVersion(event.getVersion())) {
                    int columnSearch=column - 1;
                    String lastValue=columnSearch == 0?"__":grid[index][columnSearch];
                    while (columnSearch>1 && lastValue.equals(DITTO))
                    {
                        columnSearch--;
                        lastValue=grid[index][columnSearch];
                    }
                    if (column == 1 || !lastValue.equals(dataPoint.getValue())) {
                        cell = dataPoint.getValue();
                    } else {
                        cell = DITTO;
                    }
                }
                grid[index++][column] = cell;
                maxSizes[column] = Math.max(cell.length(), maxSizes[column]);
            }
            column++;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (int c = 0; c < maxSizes.length; c++) {
            int size = maxSizes[c];
            try {
                builder.append(String.format("%" + size + "s ", (c == 0 ? FIRST_COLUMN : "" + c)));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                log.severe("Failed to create item : '%" + size + "s '");
                throw ex;
            }
        }
        builder.append("\n");
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < maxSizes.length; c++) {
                int size = maxSizes[c];
                builder.append(String.format("%"+size+"s ", grid[r][c]));
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public String[][] getGrid() {
        return grid;
    }
}
