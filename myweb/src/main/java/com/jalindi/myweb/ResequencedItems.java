package com.jalindi.myweb;

import java.util.ArrayList;
import java.util.List;

public class ResequencedItems {
    private List<ResequencedItem> resequencedItems=new ArrayList<>();
    public void add(ResequencedItem resequencedItem)
    {
        resequencedItems.add(resequencedItem);
    }

    public void add(ResequencedItems resequencedItems) {
        this.resequencedItems.addAll(resequencedItems.resequencedItems);
    }
}
