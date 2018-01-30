package com.jalindi.myweb;

import java.util.Arrays;
import java.util.Stack;

public class RepeatSequenceHelper {
 /*   public static int[] getHierarchy(String repeatKey) {
        RepeatSequence sequence=new RepeatSequence(repeatKey);
        return sequence.hierarchy;
    }*/
    public static RepeatSequence getHierarchy(String repeatKey) {
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

    public static class RepeatSequence
    {
        private String repeatPrefix;
        private int[] hierarchy;
        private RepeatSequence()
        {

        }

        public RepeatSequence(String repeatKey)
        {
            String[] paths=repeatKey.split("/");
            int start=-1;
            if (paths.length!=0) {
                for (int i = paths.length - 1; i >= 0; i--) {
                    try {
                        int path = Integer.valueOf(paths[i]);
                        start = i;
                    } catch (NumberFormatException e) {
                        break;
                    }
                }
            }
            hierarchy=new int[start<0?0:paths.length-start];
            StringBuilder builder=new StringBuilder();
            for (int i=0; i< start; i++) {
                 builder.append(paths[i]);
                builder.append("/");
            }
            repeatPrefix=builder.length()==0?null:builder.toString();
            int index=0;
            for (int i=start; i< paths.length; i++)
            {
                String path=paths[i];
                if (!path.isEmpty()) {
                    try {
                        hierarchy[index++] = Integer.valueOf(path);
                    } catch (NumberFormatException e) {
                        throw new ModelException("Invalid repeatKey " + repeatKey);
                    }
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(repeatPrefix);
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
            newRepeatSequence.hierarchy=Arrays.copyOf(hierarchy, hierarchy.length);
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
                builder.append(pathItem);
                first=false;
            }
            return builder.toString();
        }

        public String getRepeatPrefix() {
            return repeatPrefix;
         }

        public String getPrefixForLastInHierarchy() {
            if (hierarchy.length<=1)
            {
                return repeatPrefix;
            }
            return getRepeatKey(hierarchy.length-1)+"/";
        }

        public int lastInHierarchy() {
            return hierarchy[hierarchy.length-1];
        }

        public int length()
        {
            return hierarchy.length;
        }

        public void resequencePathItem(int pathItemToResequence, int repeatIndex) {
            hierarchy[pathItemToResequence]=repeatIndex;
        }
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
    public static int nextRepeat(Iterable<RepeatCoverage> repeatKeys)
    {
        int maxRepeat=0;
        for (RepeatCoverage repeatCoverage :  repeatKeys)
        {
            String repeatKey=repeatCoverage.getRepeatKey();
            RepeatSequence sequence=new RepeatSequence(repeatKey);
            maxRepeat=Math.max(maxRepeat, sequence.hierarchy[0]);
        }
        return maxRepeat+1;
    }

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
