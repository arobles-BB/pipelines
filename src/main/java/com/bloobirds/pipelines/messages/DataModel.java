package com.bloobirds.pipelines.messages;

import java.util.Map;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@RegisterForReflection
@Data
public class DataModel {
    public Map<String, String> picklistsModel;
    public Map<String, String> fieldsModel;
    public ObjectType bobjectType;
}
