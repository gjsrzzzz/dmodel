package com.jalindi.myweb;

import com.jalindi.state.DataSliceState;
import lombok.extern.java.Log;
import org.junit.Test;

@Log
public class StateTests {

    @Test
    public void test1()
    {
        createDataModel1();
    }

    private DataState createDataModel1() {
        DataSliceState slice=new DataSliceState();
        final String scope="C.A";
        slice.add(scope, "Red", "Green", "Blue");

        DataState dataState=new DataState();
        dataState.setDataSlice(slice);

        slice=slice.nextVersion();
        slice.add(scope, "Orange", "Yellow");
        dataState.setDataSlice(slice);

        ModelGrid grid=dataState.asGrid(scope);
        log.info("Model\n"+grid);

        return dataState;
    }
}
