package com.jalindi.myweb;

import lombok.AllArgsConstructor;
import lombok.Data;

public @Data @AllArgsConstructor class Event {
    private final int version;

    public static Event INFINITY=new Event(9999);
}
