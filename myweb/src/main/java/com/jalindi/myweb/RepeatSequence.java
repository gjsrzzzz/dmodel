package com.jalindi.myweb;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepeatSequence {
    private String repeatPrefix;
    private int[] hierarchy;
    private boolean isContainerHierarchy=false;
    private String CONTAINER_PREFIX="C";
    private RepeatSequence()
    {

    }

    private static final Pattern pattern= Pattern.compile("(.*)/C([0-9]*)$");
    public RepeatSequence(String repeatKey) {
        Matcher matcher = pattern.matcher(repeatKey);
        isContainerHierarchy = matcher.matches();
        if (isContainerHierarchy) {
            String first = matcher.group(1);
            String second = matcher.group(2);
            String all = matcher.group(0);
            repeatKey = first + "/" + second;
        }
        String[] paths = repeatKey.split("/");
        int start = -1;
        if (paths.length != 0) {
            for (int i = paths.length - 1; i >= 0; i--) {
                try {
                    int path = Integer.valueOf(paths[i]);
                    start = i;
                } catch (NumberFormatException e) {
                    break;
                }
            }
        }
        hierarchy = new int[start < 0 ? 0 : paths.length - start];
        if (hierarchy.length == 0) {
            throw new RuntimeException("Hierarchy must have one item in it");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < start; i++) {
            builder.append(paths[i]);
            builder.append("/");
        }
        repeatPrefix = builder.length() == 0 ? null : builder.toString();
        int index = 0;
        if (start >= 0) {
            for (int i = start; i < paths.length; i++) {
                String path = paths[i];
                if (!path.isEmpty()) {
                    try {
                        hierarchy[index++] = Integer.valueOf(path);
                    } catch (NumberFormatException e) {
                        throw new ModelException("Invalid repeatKey " + repeatKey);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(repeatPrefix);
        if (isContainerHierarchy)
        {
            builder.append(", C");
        }
        for (int i=0; i<hierarchy.length;i++)
        {
            if (builder.length()>1)
            {
                builder.append(", ");
            }
            builder.append(hierarchy[i]);
        }

        return builder.toString();
    }

    public RepeatSequence resequence(int repeatIndex) {
        RepeatSequence newRepeatSequence=new RepeatSequence();
        newRepeatSequence.repeatPrefix=repeatPrefix;
        newRepeatSequence.hierarchy= Arrays.copyOf(hierarchy, hierarchy.length);
        newRepeatSequence.hierarchy[hierarchy.length-1]=repeatIndex;
        return newRepeatSequence;
    }

    public String getRepeatKey() {
        return getRepeatKey(hierarchy.length);
    }

    public String getRepeatKey(int stopAtPathItem) {
        StringBuilder builder=new StringBuilder();
        builder.append(repeatPrefix);
        boolean first=true;
        for (int i=0; i<stopAtPathItem; i++) {
            int pathItem= hierarchy[i];
            if (!first)
            {
                builder.append("/");
            }
            if (i==0 && isContainerHierarchy)
            {
                builder.append(CONTAINER_PREFIX);
            }
            builder.append(pathItem);
            first=false;
        }
        return builder.toString();
    }

    public String getRepeatPrefix() {
        return repeatPrefix;
    }

    public String getRepeatKeyForLastInHierarchy() {
        if (hierarchy.length<=1)
        {
            return repeatPrefix.substring(0, repeatPrefix.length()-1);
        }
        return getRepeatKey(hierarchy.length-1);
    }

    public String getPrefixForLastInHierarchy() {
        if (hierarchy.length<=1)
        {
            return repeatPrefix;
        }
        return getRepeatKey(hierarchy.length-1)+"/";
    }

    public int lastInHierarchy() {
        return hierarchy.length==0?0:hierarchy[hierarchy.length-1];
    }

    public int length()
    {
        return hierarchy.length;
    }

    public ResequencedItem resequencePathItem(int pathItemToResequence, int repeatIndex) {
        String repeatKey=getRepeatKey();
        if (pathItemToResequence<0)
        {
            throw new RuntimeException("Invalid resequence");
        }
        hierarchy[pathItemToResequence]=repeatIndex;
        return new ResequencedItem(repeatKey, getRepeatKey());
    }}
