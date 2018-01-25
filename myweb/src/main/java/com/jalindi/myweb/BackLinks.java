package com.jalindi.myweb;

import lombok.extern.java.Log;

import java.util.*;

@Log
public class BackLinks {
    private final int lastVersion;
    private final LinkedList<BackLink> previousLinks=new LinkedList();
    private final LinkedList<BackLink> nextLinks=new LinkedList();

    public BackLinks(Iterable<DataPoint> previousDataPoints, Iterable<DataPoint> nextDataPoints, int version) {
        this.lastVersion=version;
        for (DataPoint point : previousDataPoints)
        {
            BackLink link=new BackLink(point.getValue(), RepeatSequenceHelper.getHierarchy(point.getRepeatKey()),
                    point.getValidFrom().getVersion(), point.getValidTo().getVersion());
            previousLinks.add(link);
        }

        for (DataPoint point : nextDataPoints)
        {
            BackLink link=new BackLink(point.getValue(), RepeatSequenceHelper.getHierarchy(point.getRepeatKey()),
                    version, Event.INFINITY.getVersion());
            nextLinks.add(link);
        }

    }

    public Collection<BackLink> process()
    {
        createBackLinksAndResequence();
        resequence();
        return nextLinks;
        // Resequence
        // Rebuild
    }



    private void resequence() {
        int repeatIndex=1;
        for (BackLink link : nextLinks) {
            link.setHierarchy(new int[] {repeatIndex});
            link.setValidFrom(link.getFirstLink().getValidFrom());
            repeatIndex++;
        }
    }

    private void createBackLinksAndResequence() {
        for (int version = lastVersion; version>=1; version--) {
            // Create back links
            createBackLinks(version);
            // Create linked list
            createLinkedList(version);
        }
    }

    private void createLinkedList(int version) {
        List<BackLink> endLinks=new ArrayList<>();
        BackLink orderParent=null;
        for (BackLink link : previousLinks) {
            if (link.coversVersion(version)) {
                if (!link.isLinked())
                {
                    if (link.getValidTo()==Event.INFINITY.getVersion()) {
                        link.setValidTo(version - 1);
                    }
                    link.setOrderParent(orderParent);
                    endLinks.add(link);
                }
                orderParent=link;
            }
        }
        for (BackLink link : endLinks) {
            BackLink finalLink=link.getOrderParent()==null?null:link.getOrderParent().getFinalLink();
            if (finalLink==null)
            {
                nextLinks.add(link);
            }
            else {
                nextLinks.add(nextLinks.indexOf(finalLink)+1, link);
            }
            previousLinks.remove(link);
        }
    }

    private void createBackLinks(int version) {
        for (BackLink link : nextLinks) {
            int index = 0;
            BackLink previous = previousLinks.size()<=index?null: previousLinks.get(index);
            BackLink linkedTo=null;
            while (previous!=null) {
                if (!previous.isLinked() &&  previous.equalsValueOf(link) &&
                        previous.coversVersion(version)) {
                    linkedTo=previous;
                    BackLink firstLink=link.getFirstLink();
                    firstLink.setBackLink(previous);
                    previous.setForwardLink(firstLink);
                    previous.setLinked(true);
                    previous=null;
                }
                else
                {
                    index++;
                    previous = index>=previousLinks.size()?null: previousLinks.get(index);
                }
            }
        }
    }

    public void log()
    {
        StringBuilder builder=new StringBuilder();
        builder.append("LINKS for version "+lastVersion+"\nnext links\n");
        for (BackLink link : nextLinks)
        {
            builder.append(link+"\n");
        }
        for (int version = lastVersion-1; version>=1; version--)
        {
            builder.append("previous links for version "+ version+ "\n");
            for (BackLink link : previousLinks)
            {
                if (!link.isLinked() && link.coversVersion(version)) {
                    builder.append(link + "\n");
                }
            }
            log.info(builder.toString());
        }
    }
}
