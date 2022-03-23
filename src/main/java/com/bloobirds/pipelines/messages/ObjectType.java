package com.bloobirds.pipelines.messages;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ObjectType {
    Opportunity, Lead, Company, Activity, Task
}
