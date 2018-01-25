package com.jalindi.myweb;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public  @Data
@RequiredArgsConstructor
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
}

