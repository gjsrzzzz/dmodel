package com.jalindi.myweb;

public class RepeatSequenceHelper {
    public static int[] getHierarchy(String repeatKey) {
        RepeatSequence sequence=new RepeatSequence(repeatKey);
        return sequence.hierarchy;
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
        private int[] hierarchy;
        public RepeatSequence(String repeatKey)
        {
            String[] paths=repeatKey.split("/");
            hierarchy=new int[paths.length==0?0:paths[0].isEmpty()?paths.length-1:paths.length];
            int index=0;
            for (String path : paths)
            {
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
    public static int nextRepeat(Iterable<String> repeatKeys)
    {
        int maxRepeat=0;
        for (String repeatKey :  repeatKeys)
        {
            RepeatSequence sequence=new RepeatSequence(repeatKey);
            maxRepeat=Math.max(maxRepeat, sequence.hierarchy[0]);
        }
        return maxRepeat+1;
    }
}
