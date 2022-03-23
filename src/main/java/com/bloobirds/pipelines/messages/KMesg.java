package com.bloobirds.pipelines.messages;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.util.ArrayList;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.extern.java.Log;
import org.apache.camel.Exchange;

@RegisterForReflection
@Data
@Log
public class KMesg {
    public Action action; // ENUM
    public String accountId; // USER
    public ObjectType bobjectType; // ENUM
    public RawObject beforeBobject;
    public RawObject afterBobject;
    public FrozenModel frozenModel;
    public ArrayList<RawObject> relatedBobjects;

    public boolean isCompany() {return bobjectType.equals(ObjectType.Company);}
    public boolean isLead() {return bobjectType.equals(ObjectType.Lead);}
}
