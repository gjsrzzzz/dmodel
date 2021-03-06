package com.jalindi.myweb;

import lombok.extern.java.Log;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Log
public class SeqHistoryTests
{
    @Test
    public void testHistory()
    {
        DataPointHistory model= createDataModel1();
        ModelGrid grid=model.asGrid();
        log.info("Model\n"+grid);
        log.info(""+model);
        assertArrayEquals(new String[][] {
                {"/1","Red","--"},
                {"/2","Green", ""},
                {"/3","Blue","--"},
                {"/4","","Orange"}}, grid.getGrid());
    }

    @Test
    public void testHistory2()
    {
        DataPointHistory model= createDataModel2();
        ModelGrid grid=model.asGrid();
        log.info("Model\n"+grid);
        log.info(""+model);
        assertArrayEquals(new String[][] {
                {"/1","","Red","--","--"},
                {"/2","","Blue", "", "Orange"},
                {"/3","Green","--","--","--"},
                {"/4","","","Yellow","--"}},
                grid.getGrid());
    }

    private DataPointHistory createDataModel1() {
        DataPointHistory model=new DataPointHistory();
        model.add("Red", "Green", "Blue");

        DataPointHistory nextModel=model.nextVersion();
        nextModel.remove("Green");
        nextModel.add("Orange");
        nextModel.resequence();
        log.info(""+nextModel);
        model.merge(nextModel);
        return model;
    }

    private DataPointHistory createDataModel2() {
        DataPointHistory model=new DataPointHistory();
        model.add("Green");

        DataPointHistory nextModel=model.nextVersion();
        nextModel.remove("Green");
        nextModel.add("Red","Blue","Green");
        assertEquals(3, nextModel.size());
        nextModel.resequence();
        assertEquals(3, nextModel.size());
        model.merge(nextModel);

       nextModel=model.nextVersion();
        nextModel.remove("Blue");
        nextModel.add("Yellow");
        nextModel.resequence();
        model.merge(nextModel);

       nextModel=model.nextVersion();
        nextModel.addAfter("Red","Orange");
        nextModel.resequence();
        model.merge(nextModel);
        return model;
    }

    @Test
    public void testHistory3()
    {
        DataPointHistory model= createDataModel3();
        ModelGrid grid=model.asGrid();
        log.info("Model\n"+grid);
        log.info(""+model);
        assertArrayEquals(new String[][] {
                        {"/1","","Red","","Red"},
                        {"/2","","Blue", "", "Orange"},
                        {"/3","Green","--","--","--"},
                        {"/4","","","Yellow","--"}},
                grid.getGrid());
    }

    private DataPointHistory createDataModel3() {
        DataPointHistory model=new DataPointHistory();
        model.add("Green");

        DataPointHistory nextModel=model.nextVersion();
        nextModel.remove("Green");
        nextModel.add("Red","Blue","Green");
        nextModel.resequence();
        model.merge(nextModel);

        nextModel=model.nextVersion();
        nextModel.remove("Red","Blue");
        nextModel.add("Yellow");
        nextModel.resequence();
        model.merge(nextModel);

        ModelGrid grid=model.asGrid();
        log.info("Model\n"+grid);
        nextModel=model.nextVersion();
        nextModel.addAfter(null,"Red","Orange");
        nextModel.resequence();
        model.merge(nextModel);
        return model;
    }


    @Test
    public void testHistoryHierarchy()
    {
        DataPointHistory model= createDataModelHierarchy();
        ModelGrid grid=model.asGrid();
        log.info("Model\n"+grid);
        log.info(""+model);
        assertArrayEquals(new String[][] {
                        {"/CP/1","Blue","--"},
                        {"/CP/2","Green", "--"},
                        {"/JP/1","Red","--"},
                        {"/JP/2","Green","--"},
                        {"/JP/3","","Blue"}},
                grid.getGrid());
    }

    private DataPointHistory createDataModelHierarchy() {
        DataPointHistory model=new DataPointHistory();
     //   model.addWithRepeat("Green", "/JP/1");
        model.addAtRepeat("/CP", "Blue");
        model.addAtRepeat("/CP","Green" );
        model.addAtRepeat("/JP","Red" );
        model.addAtRepeat("/JP","Green" );

        DataPointHistory nextModel=nextModel=model.nextVersion();
        nextModel.addAtRepeat("/JP","Blue" );
        model.merge(nextModel);
        return model;
    }

    @Test
    public void testPartyHierarchy()
    {
        DataPointHistory model= createDataModelPartyHierarchy();
        ModelGrid grid=model.asGrid();
        log.info("Model\n"+grid);
        log.info(""+model);
        assertArrayEquals(new String[][] {
                        {"/1","","IM0"},
                        {"/2","IM1", "--"},
                        {"/2/1","CP1","--"},
                        {"/2/2","CP2","--"},
                        {"/3","IM2","--"},
                        {"/4","CP3","--"}},
                grid.getGrid());
    }

    private DataPointHistory createDataModelPartyHierarchy() {
        DataPointHistory model=new DataPointHistory();
        model.addAtRepeat("/", "IM1");
        model.addAtRepeat("/1","CP1" );
        model.addAtRepeat("/1","CP2" );
        model.addAtRepeat("/","IM2" );
        model.addAtRepeat("/","CP3" );
        ModelGrid grid=model.asGrid();
        log.info("Model\n"+grid);

        DataPointHistory nextModel=nextModel=model.nextVersion();
   //    nextModel.add("IM0" );
        nextModel.addAfter(null,"IM0" );
        model.merge(nextModel);
        return model;
    }
}
