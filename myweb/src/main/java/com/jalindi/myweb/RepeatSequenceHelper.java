package com.jalindi.myweb;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepeatSequenceHelper {
 /*   public static int[] getHierarchy(String repeatKey) {
        RepeatSequence sequence=new RepeatSequence(repeatKey);
        return sequence.hierarchy;
    }*/
    public static RepeatSequence createRepeatSequence(String repeatKey) {
        if (repeatKey==null || repeatKey.length()==0)
        {
            repeatKey="/1";
        }
        RepeatSequence sequence=new RepeatSequence(repeatKey);
        return sequence;
    }

    public static String toRepeatKey(int[] hierarchy) {
        StringBuilder builder=new StringBuilder();
        for (int pathItem : hierarchy) {
            builder.append("/"+pathItem);
        }
        return builder.toString();
    }

  /*  public static int nextRepeat(Iterable<String> repeatKeys)
    {
        int maxRepeat=0;
        for (String repeatKey :  repeatKeys)
        {
            RepeatSequence sequence=new RepeatSequence(repeatKey);
            maxRepeat=Math.max(maxRepeat, sequence.hierarchy[0]);
        }
        return maxRepeat+1;
    }*/

    public static int nextRepeat(String repeatPrefix, Iterable<RepeatCoverage> repeatKeys)
    {
        int maxRepeat=0;
        for (RepeatCoverage repeatCoverage :  repeatKeys)
        {
            String repeatKey=repeatCoverage.getRepeatKey();
            RepeatSequence sequence=new RepeatSequence(repeatKey);
            String sequencePrefix=sequence.getPrefixForLastInHierarchy();
            if (sequencePrefix.equals(repeatPrefix)) {
                maxRepeat = Math.max(maxRepeat, sequence.lastInHierarchy());
            }
        }
        return maxRepeat+1;
    }
}
