package com.jalindi.myweb;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public  @Data
@RequiredArgsConstructor
@Log
class BackLink
{
    private @NonNull
    String value;
    private @NonNull int[] hierarchy;
    private @NonNull int validFrom;
    private @NonNull int validTo;
    private BackLink backLink=null;
    private BackLink forwardLink=null;
    private BackLink orderParent=null;
    private List<BackLink> mergedWith=new ArrayList<>();
    private boolean isMergedIntoOther=false;

    private boolean linked=false;
    public void setValidTo(int validTo)
    {
        if (validTo<validFrom)
        {
            throw new ModelException("Cannot set valid to before valid from "+validTo);
        }
        this.validTo=validTo;
    }
    public void setBackLink(BackLink link)
    {
        if (link==this)
        {
            throw new ModelException("Cannot link to itself "+link);
        }
        this.backLink=link;
    }
    @Override
    public String toString()
    {
        StringBuilder builder=new StringBuilder();
        builder.append("[");
        for (int i=0; i<hierarchy.length;i++)
        {
            if (builder.length()>1)
            {
                builder.append(", ");
            }
            builder.append(hierarchy[i]);
        }
        builder.append("]");
        return String.format("%15s", value)+builder+"("+validFrom+"-"+validTo+") "+(linked?"linked ":"")+(backLink==null?"":"backlink="+backLink);
    }

    public boolean coversVersion(int version) {
        return validFrom<=version && validTo >= version;
    }

    public boolean equalsValueOf(BackLink otherLink) {
        return value.equals(otherLink.value);
    }

    public BackLink getFinalLink() {
        return forwardLink==null?this:forwardLink.getFinalLink();
    }

    public BackLink getFirstLink() {
        return backLink==null?this:backLink.getFirstLink();
    }

    public boolean mergeInto(int lastVersion, BackLink previousLink) {
        if (previousLink!=null)
        {
            boolean[] thisCoverage = getCoverage(lastVersion);
            boolean[] previousCoverage = previousLink.getCoverage(lastVersion);
            StringBuilder builder=new StringBuilder();
            builder.append("Coverage\n");
            String previousValue=previousLink.getAllValues();
            String thisValue=getAllValues();
            int valueLength=Math.max(previousValue.length(), thisValue.length());
            builderCoverageString(builder,valueLength, previousValue, previousCoverage);
            addCoverageToBuilder(builder, valueLength,thisValue, thisCoverage);
            log.info(builder.toString());
            boolean clash=false;
            for (int version=0; version<thisCoverage.length || version<previousCoverage.length; version++)
            {
                boolean versionCoveredThis=version<thisCoverage.length?thisCoverage[version]:false;
                boolean versionCoveredPrevious=version<previousCoverage.length?previousCoverage[version]:false;
                if (versionCoveredThis && versionCoveredPrevious)
                {
                    clash=true;
                }
            }
            if (!clash)
            {
                log.info("Links can be merged");
                isMergedIntoOther=true;
                previousLink.mergedWith.add(this);
                return true;
            }
        }
        return false;
     }

    private void logCoverage(int lastVersion) {
        boolean[] coverage=getCoverage(lastVersion);
        StringBuilder builder=new StringBuilder();
        builder.append("Coverage\n");
        builderCoverageString(builder, value.length(), value, coverage);
        log.info(builder.toString());
    }

    private static void builderCoverageString(StringBuilder builder, int valueLength, String value, boolean[] coverage) {
        builder.append(String.format("%"+valueLength+"s", ""));
        for (int version=0; version<coverage.length; version++)
        {
            builder.append(String.format("%3s", ""+version));
        }
        addCoverageToBuilder(builder, valueLength, value, coverage);
    }

    private static void addCoverageToBuilder(StringBuilder builder, int valueLength, String value, boolean[] coverage) {
        builder.append("\n");
        builder.append(String.format("%"+valueLength+"s", value));
        for (int version=0; version<coverage.length; version++)
        {
            builder.append(String.format("%3s",coverage[version]?"T":" "));
        }
    }

    public boolean[] getCoverageWithoutMerged() {
        int lastVersion=validTo==Event.INFINITY.getVersion()?validFrom:validTo;
        boolean[] coverage=new boolean[lastVersion+1];
        addCoverage(lastVersion, coverage, this);
        return coverage;
    }

        public boolean[] getCoverage(int lastVersion) {
        boolean[] coverage=new boolean[lastVersion+1];
        addCoverage(lastVersion, coverage, this);
        if (mergedWith!=null) {
            for (BackLink link : mergedWith) {
                addCoverage(lastVersion, coverage, link);
            }
        }
        return coverage;
    }

    private String getAllValues()
    {
        if (mergedWith.size()==0)
        {
            return value;
        }
        StringBuilder builder=new StringBuilder();
        builder.append(value);
        for (BackLink link : mergedWith)
        {
            builder.append(", ");
            builder.append(link.value);
        }
        return builder.toString();
    }

    private static void addCoverage(int lastVersion, boolean[] coverage, BackLink link) {
        while (link!=null)
        {
            for (int version=link.validFrom; version<=lastVersion && version<=link.validTo; version++)
            {
                coverage[version]=true;
            }
            link=link.backLink;
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
return super.hashCode();
    }
}

