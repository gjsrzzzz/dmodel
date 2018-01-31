package com.jalindi.myweb;

import com.jalindi.state.DataSliceState;
import lombok.extern.java.Log;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

@Log
public class StateTests {
    final String scope="C.A";

    @Test
    public void testSimpleState()
    {
        DataState state = createDataModel1();
        ModelGrid grid=state.asGrid(scope);
        assertArrayEquals(new String[][] {
                        {"/1","","","Black"},
                        {"/2","Red","--", "--"},
                        {"/3","Green","",""},
                        {"/4","Blue","--",""},
                        {"/5","","Orange",""},
                        {"/6","","Yellow","--"},
                        {"/7","","","Pink"}},
                grid.getGrid());
    }

    private DataState createDataModel1() {
        DataSliceState slice=new DataSliceState();
        slice.add(scope, "Red", "Green", "Blue");

        DataState dataState=new DataState();
        dataState.setDataSlice(slice);

        slice=slice.nextVersion();
        slice.remove(scope, "Green");
        slice.add(scope, "Orange", "Yellow");
        dataState.setDataSlice(slice);

        slice=slice.nextVersion();
        slice.remove(scope, "Blue", "Orange");
        slice.add(scope, "Pink");
        slice.addAfter(scope, null, "Black");
        dataState.setDataSlice(slice);

        ModelGrid grid=dataState.asGrid(scope);
        log.info("Model\n"+grid);

        return dataState;
    }
}
