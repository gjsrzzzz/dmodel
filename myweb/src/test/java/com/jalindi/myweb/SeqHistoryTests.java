package com.jalindi.myweb;

import lombok.extern.java.Log;
import org.junit.Test;
import org.junit.Assert.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Log
public class SeqHistoryTests
{
    @Test
    public void testHistory()
    {
        DataModel model= createDataModel1();
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
        DataModel model= createDataModel2();
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

    private DataModel createDataModel1() {
        DataModel model=new DataModel();
        model.add("Red", "Green", "Blue");

        DataModel nextModel=model.nextVersion();
        nextModel.remove("Green");
        nextModel.add("Orange");
        nextModel.resequence();
        log.info(""+nextModel);
        model.merge(nextModel);
        return model;
    }

    private DataModel createDataModel2() {
        DataModel model=new DataModel();
        model.add("Green");

        DataModel nextModel=model.nextVersion();
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
        DataModel model= createDataModel3();
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

    private DataModel createDataModel3() {
        DataModel model=new DataModel();
        model.add("Green");

        DataModel nextModel=model.nextVersion();
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
        DataModel model= createDataModelHierarchy();
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

    private DataModel createDataModelHierarchy() {
        DataModel model=new DataModel();
     //   model.addWithRepeat("Green", "/JP/1");
        model.addAtRepeat("/CP", "Blue");
        model.addAtRepeat("/CP","Green" );
        model.addAtRepeat("/JP","Red" );
        model.addAtRepeat("/JP","Green" );

        DataModel nextModel=nextModel=model.nextVersion();
        nextModel.addAtRepeat("/JP","Blue" );
        model.merge(nextModel);
        return model;
    }

    @Test
    public void testPartyHierarchy()
    {
        DataModel model= createDataModelPartyHierarchy();
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

    private DataModel createDataModelPartyHierarchy() {
        DataModel model=new DataModel();
        model.addAtRepeat("/", "IM1");
        model.addAtRepeat("/1","CP1" );
        model.addAtRepeat("/1","CP2" );
        model.addAtRepeat("/","IM2" );
        model.addAtRepeat("/","CP3" );
        ModelGrid grid=model.asGrid();
        log.info("Model\n"+grid);

        DataModel nextModel=nextModel=model.nextVersion();
   //    nextModel.add("IM0" );
        nextModel.addAfter(null,"IM0" );
        model.merge(nextModel);
        return model;
    }
}
