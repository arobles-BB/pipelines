package com.bloobirds.pipelines.messages;


import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.extern.java.Log;

import java.util.ArrayList;

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
    public boolean isActivity() {return bobjectType.equals(ObjectType.Activity);}
    public boolean isTask() {return bobjectType.equals(ObjectType.Task);}
    public boolean isOpportunity() {return bobjectType.equals(ObjectType.Opportunity);}
}
