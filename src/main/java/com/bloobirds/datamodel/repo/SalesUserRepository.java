package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.SalesUser;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SalesUserRepository implements PanacheRepositoryBase<SalesUser, BBObjectID> {

    public SalesUser findById(String tenantID, String BBobjectID){
        BBObjectID id= new BBObjectID();
        id.setBBobjectID(BBobjectID);
        id.setTenantID(tenantID);
        return findById(id);
    }
}
