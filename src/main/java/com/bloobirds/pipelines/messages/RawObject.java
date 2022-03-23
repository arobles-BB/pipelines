package com.bloobirds.pipelines.messages;

import java.time.LocalDateTime;
import java.util.Map;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@RegisterForReflection
@Data
public class RawObject {
    public Id id;
    public Map<String, String> contents;
//    private Map<String, LocalDateTime> contentsDate;
//    private Map<String, Long> contentsLong;
//    private Map<String, String> contentsID;
    public boolean opportunity;
    public boolean lead;
    public boolean company;
    public boolean activity;
    public boolean task;
}
