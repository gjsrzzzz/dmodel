package com.jalindi.myweb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.List;

public @Data
@AllArgsConstructor @EqualsAndHashCode
class RepeatCoverage {
    private @NonNull final String repeatKey;
    private @NonNull final Event validFrom;
    private @NonNull final Event validTo;

    @Override
    public String toString() {
        return repeatKey + " (" + validFrom.getVersion() + "-" + validTo.getVersion() + ")";
    }

    public boolean coversVersion(int version) {
        return validFrom.getVersion() <= version && validTo.getVersion() >= version;
    }

    public static RepeatCoverage create(List<Event> events, String repeatKey) {
        Event validFrom=events==null||events.size()==0?new Event(0):events.get(0);
        Event validTo=Event.INFINITY;
        if (events.size()>1)
        {
            validTo=events.get(events.size()-1);
        }
        return new RepeatCoverage(repeatKey, validFrom, validTo);
    }
}
