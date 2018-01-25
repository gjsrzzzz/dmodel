package com.jalindi.myweb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

public @Data @AllArgsConstructor class DataPoint {
    private @NonNull final String value;
    private @NonNull final String repeatKey;
    private @NonNull final Event validFrom;
    private @NonNull final Event validTo;

    @Override
    public String toString()
    {
        return value+"["+repeatKey+"]("+validFrom.getVersion()+"-"+validTo.getVersion()+")";
    }

    public boolean coversVersion(int version) {
        return validFrom.getVersion()<=version && validTo.getVersion() >= version;
    }
}
