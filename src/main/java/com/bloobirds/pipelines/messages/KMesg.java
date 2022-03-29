package com.bloobirds.pipelines.messages;


import com.bloobirds.datamodel.abstraction.LogicRoles;
import com.bloobirds.datamodel.abstraction.logicroles.OpportunityLogicRoles;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static Map<String, String> flipHashMap(Map<String, String> map) {
        Map<String, String> result = new HashMap<>();
        map.forEach((k, v) -> result.put(v, k));
        return result;
    }

    public static String findField(KMesg data, Map<String, String> flippedFieldsModel, LogicRoles lrole) {
        String result = "";
        String fID = flippedFieldsModel.get(lrole.name());
        if (fID != null) result = data.afterBobject.contents.get(fID);
        return result;
    }

    public static String findPicklist(KMesg data, Map<String, String> picklistsModel, String lroleID) {
        String value=picklistsModel.get(lroleID);
        if(value==null) value=lroleID;
        return value;
    }

}
