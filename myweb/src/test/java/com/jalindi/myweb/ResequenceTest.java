package com.jalindi.myweb;

import com.jalindi.state.Container;
import com.jalindi.state.DataSliceState;
import lombok.extern.java.Log;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

@Log
public class ResequenceTest
{
    final String containerScope="C";
    final String scope1="C.A";
    final String scope2="C.B";

    @Test
    public void testRequencer()
    {
        DataState state = createDataModel2("/C1", "/C2");
        String serialC1 = state.getContainerSerial("C","/C1");
        String serialC2 =state.getContainerSerial("C","/C2");
                assertArrayEquals(new String[][] {
                        {"C.A","/C1/1","Red"},
                        {"C.A","/C2/1","Green"},
                        {"C.B","/C1/1","Blue"},
                        {"C.B","/C2/1","Yellow"},
                        {"C.serial","/C1",serialC1},
                        {"C.serial","/C2",serialC2}
                        },
                state.asGrid());
        DataSliceState nextSlice = state.getLastSlice().nextVersion();
        nextSlice.addContainerAt("C","/C", 1);
        nextSlice.addAfter("C.B","/C2","Yellow", "Black", "White");
        state.setDataSlice(nextSlice);
        state.log();
    }

    @Test
    public void containerTest()
    {
        Container container1= new Container("C1/");
        log.info(container1.toString());
        Container container2= new Container("C1/");
        log.info(container2.toString());
    }

    private DataState createDataModel2(String firstRepeatKey, String secondRepeatKey) {
        DataSliceState slice=new DataSliceState();
        slice.addContainer(containerScope, firstRepeatKey);
        slice.add(scope1, firstRepeatKey,"Red");
        slice.add(scope2, firstRepeatKey,"Blue");
        slice.addContainer(containerScope, secondRepeatKey);
        slice.add(scope1, secondRepeatKey,"Green");
        slice.add(scope2, secondRepeatKey,"Yellow");

        DataState dataState=new DataState();
        dataState.setDataSlice(slice);
        slice=slice.nextVersion();

        dataState.log();
        return dataState;
    }
}
