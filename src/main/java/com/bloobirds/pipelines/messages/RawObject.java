package com.bloobirds.pipelines.messages;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.Map;

@RegisterForReflection
@Data
public class RawObject {
    public Id id;
    public Map<String, String> contents;
    public boolean opportunity;
    public boolean lead;
    public boolean company;
    public boolean activity;
    public boolean task;
}
