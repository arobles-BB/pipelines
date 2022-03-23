package com.bloobirds.pipelines.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@RegisterForReflection
@Data
public class FrozenModel {
    @JsonProperty("Lead")
    public DataModel lead;
    @JsonProperty("Company")
    public DataModel company;
    @JsonProperty("Opportunity")
    public DataModel opportunity;
    @JsonProperty("Activity")
    public DataModel  activity;
    @JsonProperty("Task")
    public DataModel  task;
}

