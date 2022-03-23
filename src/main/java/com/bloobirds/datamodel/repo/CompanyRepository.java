package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.Company;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.pipelines.messages.KMesg;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CompanyRepository implements PanacheRepositoryBase<Company, BBObjectID> {

    public Company newCompanyFromKMsg(KMesg data){
        Company c= new Company();


        return c;
    }

}
