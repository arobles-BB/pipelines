package com.bloobirds.pipelines.messages;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@RegisterForReflection
@Data
public class Id {
    public String value; // FULL PATH <ACCOUNT/TYPE/ID>
    public ObjectType typeName; // ENUM
    public String accountId; // USER
    public String objectId; // ID
}
