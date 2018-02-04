package com.jalindi.myweb;

import com.jalindi.state.DataSliceState;
import lombok.extern.java.Log;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

@Log
public class ResequenceTest
{
    final String scope1="C.A";
    final String scope2="C.B";

    @Test
    public void testRequencer()
    {
        DataState state = createDataModel2("/C1", "/C2");
        assertArrayEquals(new String[][] {
                        {"/C1/1","","","Black"},
                        {"/C1/2","Red","--", "--"},
                        {"/C1/3","Green","",""},
                        {"/C1/4","Blue","--","Pink"},
                        {"/C2/1","","Orange",""},
                        {"/C2/2","","Yellow","--"}},
                state.asGrid(scope1).getGrid());
        DataSliceState slice= state.getDataSlice(1);
        log.info(slice.toString());
        slice.add(scope1,"/C2","Purple");
        state.setDataSlice(slice);
        ModelGrid grid=state.asGrid(scope1);
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

    private DataState createDataModel2(String baseRepeatKey, String secondRepeatKey) {
        DataSliceState slice=new DataSliceState();
        slice.add(scope1, baseRepeatKey,"Red", "Green", "Blue");

        DataState dataState=new DataState();
        dataState.setDataSlice(slice);

        slice=slice.nextVersion();
        slice.remove(scope1, baseRepeatKey, "Green");
        slice.add(scope1, secondRepeatKey, "Orange", "Yellow");
        dataState.setDataSlice(slice);
        assertArrayEquals(new String[][] {
                        {baseRepeatKey+"/1","Red","--"},
                        {baseRepeatKey+"/2","Green",""},
                        {baseRepeatKey+"/3","Blue","--"},
                        {secondRepeatKey+"/1","","Orange"},
                        {secondRepeatKey+"/2","","Yellow"}},
                dataState.asGrid(scope1).getGrid());

        slice=slice.nextVersion();
        slice.remove(scope1, baseRepeatKey, "Blue");
        slice.remove(scope1, secondRepeatKey, "Orange");
        slice.add(scope1, baseRepeatKey, "Pink");
        slice.addAfter(scope1, baseRepeatKey, null, "Black");
        dataState.setDataSlice(slice);

        ModelGrid grid=dataState.asGrid(scope1);
        log.info("Model\n"+grid);

        return dataState;
    }
}
