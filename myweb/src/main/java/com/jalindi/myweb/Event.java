package com.jalindi.myweb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

public @Data @AllArgsConstructor @EqualsAndHashCode
class Event {
    private final int version;

    public static Event INFINITY=new Event(9999);
}
