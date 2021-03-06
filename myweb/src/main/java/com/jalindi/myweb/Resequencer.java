package com.jalindi.myweb;

import java.util.*;

public class Resequencer {
    private List<BackLink> links;
    private Map<String, ResequenceData> resequenceDataMap=new HashMap<>();


    public class ResequenceData
    {
        String prefix;
        int repeatIndex=1;
        BackLink previousLink=null;
        List<BackLink> children=new ArrayList<>();

        public ResequencedItems resequence(BackLink link) {
            ResequencedItems resequenceItems=new ResequencedItems();
            int pathItemToResequence = link.getRepeatSequence().length()-1;
            String repeatKeyPrefix=link.getRepeatSequence().getRepeatKey()+"/";
            ResequenceData dataForChildren=getOrCreateResequenceData(repeatKeyPrefix);
            resequenceItems.add(link.getRepeatSequence().resequencePathItem(pathItemToResequence, repeatIndex));
            for (BackLink child : dataForChildren.children)
            {
                resequenceItems.add(child.getRepeatSequence().resequencePathItem(pathItemToResequence, repeatIndex));
            }
            return resequenceItems;
        }
    }

    public Resequencer(List<BackLink> links)
    {
        this.links=links;
        createMap(links);
    }

    public Resequencer(Collection<DataPointValue> dataPointValues, int version) {
        links=new LinkedList<>();
        for (DataPointValue point : dataPointValues) {
            BackLink link = new BackLink(point.getValue(), RepeatSequenceHelper.createRepeatSequence(point.getRepeatKey()),
                    point.getValidFrom().getVersion(), point.getValidTo().getVersion());
            //   version, Event.INFINITY.getVersion());
            links.add(link);
        }
        createMap(links);
    }

    private void createMap(List<BackLink> links) {
        for (BackLink link : links) {
            String prefix=link.getRepeatSequence().getPrefixForLastInHierarchy();
            ResequenceData resequenceData = getOrCreateResequenceData(prefix);
            resequenceData.prefix=prefix;
            if (prefix!=null && prefix.length()>1) {
                resequenceData.children.add(link);
            }
        }
    }

    private ResequenceData getOrCreateResequenceData(String prefix) {
        ResequenceData resequenceData=resequenceDataMap.get(prefix);
        if (resequenceData==null)
        {
            resequenceData=new ResequenceData();
            resequenceDataMap.put(prefix, resequenceData);
        }
        return resequenceData;
    }

    public ResequencedItems resequence(int lastVersion) {
        ResequencedItems resequenceDataList=new ResequencedItems();
        for (ListIterator<BackLink> iterator = links.listIterator(); iterator.hasNext(); ) {
            BackLink link = iterator.next();
            Resequencer.ResequenceData resequenceData = getOrCreateResequenceData(link.getRepeatSequence().getPrefixForLastInHierarchy());
            boolean wasMergedPrevious = lastVersion>0 && link.mergeInto(lastVersion, resequenceData.previousLink);
            if (wasMergedPrevious) {
                iterator.remove();
            } else {
                int currentSequence = link.getRepeatSequence().lastInHierarchy();
                if (currentSequence != resequenceData.repeatIndex) {
                    resequenceDataList.add(resequenceData.resequence(link));
                    //              link.setValidFrom(link.getFirstLink().getValidFrom());
                }
                resequenceData.repeatIndex++;
                resequenceData.previousLink = link;
            }
        }
        return resequenceDataList;
    }

    public void resequence(int lastVersion, String afterValue, String... values) {
        List<BackLink> fromList=links;
        links=new LinkedList<>();
        if (afterValue == null) {
            Resequencer.ResequenceData resequenceData = getOrCreateResequenceData("/");
            resequenceData.repeatIndex = addValues(lastVersion, resequenceData.repeatIndex, "/", values);
        }
        for (BackLink link : fromList ) {
            Resequencer.ResequenceData resequenceData = getOrCreateResequenceData(link.getRepeatSequence().getPrefixForLastInHierarchy());

            int currentSequence = link.getRepeatSequence().lastInHierarchy();
            if (currentSequence != resequenceData.repeatIndex) {
                resequenceData.resequence(link);
                //              link.setValidFrom(link.getFirstLink().getValidFrom());
            }
            links.add(link);
            resequenceData.repeatIndex++;
            if (link.getValue().equals(afterValue)) {
                resequenceData.repeatIndex = addValues(lastVersion,resequenceData.repeatIndex, resequenceData.prefix, values);
            }

        }
    }

    private int addValues( int version, int repeatIndex, String repeatPrefix, String[] values ) {
        if (values!=null) {
            for (String value : values) {
                BackLink backLink = new BackLink(value,
                        RepeatSequenceHelper.createRepeatSequence(repeatPrefix + repeatIndex),
                        version, Event.INFINITY.getVersion());
                links.add(backLink);
                repeatIndex++;
            }
        }
        return repeatIndex;
    }

    public List<BackLink> getLinks() {
        return links;
    }
}
