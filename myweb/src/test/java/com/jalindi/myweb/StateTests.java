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
        DataState state = createDataModel1("");
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

    @Test
    public void testSimpleStateInContainer()
    {
        DataState state = createDataModel1("/C1");
        ModelGrid grid=state.asGrid(scope);
        assertArrayEquals(new String[][] {
                        {"/C1/1","","","Black"},
                        {"/C1/2","Red","--", "--"},
                        {"/C1/3","Green","",""},
                        {"/C1/4","Blue","--",""},
                        {"/C1/5","","Orange",""},
                        {"/C1/6","","Yellow","--"},
                        {"/C1/7","","","Pink"}},
                grid.getGrid());
    }

    @Test
    public void testMultiStateInContainer()
    {
        DataState state = createDataModel2("/C1", "/C2");
        DataSliceState slice= state.getDataSlice(1);
        log.info(slice.toString());
        slice.add(scope,"/C2","Purple");
        state.setDataSlice(slice);
        ModelGrid grid=state.asGrid(scope);
        log.info("Model\n"+grid);
        assertArrayEquals(new String[][] {
                        {"/C1/1","","","Black"},
                        {"/C1/2","Red","--", "--"},
                        {"/C1/3","Green","",""},
                        {"/C1/4","Blue","--","Pink"},
                        {"/C2/1","","Orange",""},
                        {"/C2/2","","Yellow","--"},
                        {"/C2/3","","Purple",""}},
                grid.getGrid());
    }


    private DataState createDataModel1(String baseRepeatKey) {
        DataSliceState slice=new DataSliceState();
        slice.add(scope, baseRepeatKey,"Red", "Green", "Blue");

        DataState dataState=new DataState();
        dataState.setDataSlice(slice);

        slice=slice.nextVersion();
        slice.remove(scope, baseRepeatKey, "Green");
        slice.add(scope, baseRepeatKey, "Orange", "Yellow");
        dataState.setDataSlice(slice);

        slice=slice.nextVersion();
        slice.remove(scope, baseRepeatKey, "Blue", "Orange");
        slice.add(scope, baseRepeatKey, "Pink");
        slice.addAfter(scope, baseRepeatKey, null, "Black");
        dataState.setDataSlice(slice);

        ModelGrid grid=dataState.asGrid(scope);
        log.info("Model\n"+grid);

        return dataState;
    }

    private DataState createDataModel2(String baseRepeatKey, String secondRepeatKey) {
        DataSliceState slice=new DataSliceState();
        slice.add(scope, baseRepeatKey,"Red", "Green", "Blue");

        DataState dataState=new DataState();
        dataState.setDataSlice(slice);

        slice=slice.nextVersion();
        slice.remove(scope, baseRepeatKey, "Green");
        slice.add(scope, secondRepeatKey, "Orange", "Yellow");
        dataState.setDataSlice(slice);

        slice=slice.nextVersion();
        slice.remove(scope, baseRepeatKey, "Blue");
        slice.remove(scope, secondRepeatKey, "Orange");
        slice.add(scope, baseRepeatKey, "Pink");
        slice.addAfter(scope, baseRepeatKey, null, "Black");
        dataState.setDataSlice(slice);

        ModelGrid grid=dataState.asGrid(scope);
        log.info("Model\n"+grid);

        return dataState;
    }
}
