package com.bloobirds.pipelines.messages;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@RegisterForReflection
public enum ObjectType {
    Opportunity, Lead, Company, Activity, Task;
}
