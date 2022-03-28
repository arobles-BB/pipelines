package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.Company;
import com.bloobirds.datamodel.Contact;
import com.bloobirds.datamodel.SalesUser;
import com.bloobirds.datamodel.abstraction.Activity;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.logicroles.CompanyLogicRoles;
import com.bloobirds.datamodel.abstraction.logicroles.ContactLogicRoles;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.pipelines.messages.Action;
import com.bloobirds.pipelines.messages.KMesg;
import com.bloobirds.pipelines.messages.RawObject;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import lombok.extern.java.Log;
import org.apache.camel.Body;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<Contact, BBObjectID> {

    @Inject
    CompanyRepository companyRepo;

    @Inject
    SalesUserRepository salesUserRepo;

    @Inject
    ActivityRepository activityRepo;

    //@todo no hay protección a nulls!

    private static Map<String, String> flipHashMap(Map<String, String> map) {
        Map<String, String> result = new HashMap<>();
        map.forEach((k, v) -> result.put(v, k));
        return result;
    }

    private static String findField(KMesg data, Map<String, String> flippedFieldsModel, ContactLogicRoles lrole) {
        String result = "";
        String fID = flippedFieldsModel.get(lrole.name());
        if (fID != null) result = data.afterBobject.contents.get(fID);
        return result;
    }

    private static String findPicklist(KMesg data, String lroleID) {
        String value=data.frozenModel.lead.picklistsModel.get(lroleID);
        if(value==null) value=lroleID;
        return value;
    }

    @Transactional
    public Contact createContactFromKMesg(@Body KMesg data) {

        Map<String, String> flippedFieldsModel = flipHashMap(data.frozenModel.lead.fieldsModel);

        Contact c;

        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        id.setBBobjectID(data.afterBobject.id.objectId);
        c=findById(id);
        if (data.action.equals(Action.DELETE)) {
            if (c!=null) {
                activityRepo.deleteByContact(c);
                delete(c);
            }
            return c;
        }
        if(c==null){
            c=new Contact();
            c.objectID = id;
        }

        Company co;
        String[] parts = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__COMPANY).split("/");
        if (parts.length != 0)
        {
            BBObjectID coid = new BBObjectID();
            coid.setTenantID(data.accountId);
            coid.setBBobjectID(parts[parts.length - 1]);
            co = companyRepo.findById(coid);
            if(co == null) {
                co = new Company();
                co.objectID = coid;

                int i=0;
                while(!data.relatedBobjects.get(i).isCompany()) i++;
                RawObject relatedCo=data.relatedBobjects.get(i);

                Map<String,String> flippedCompanyModel= flipHashMap(data.frozenModel.company.fieldsModel);

                String fieldID=flippedCompanyModel.get(CompanyLogicRoles.COMPANY__NAME.name());
                co.name = relatedCo.contents.get(fieldID);
                fieldID=flippedCompanyModel.get(CompanyLogicRoles.COMPANY__DISCARDED_REASONS.name());
                co.discardedReasons = relatedCo.contents.get(fieldID);
                fieldID=flippedCompanyModel.get(CompanyLogicRoles.COMPANY__NURTURING_REASONS.name());
                co.nurturingReasons = relatedCo.contents.get(fieldID);
                fieldID=flippedCompanyModel.get(CompanyLogicRoles.COMPANY__TARGET_MARKET.name());
                co.targetMarket = relatedCo.contents.get(fieldID);
                fieldID=flippedCompanyModel.get(CompanyLogicRoles.COMPANY__COUNTRY.name());
                co.country = relatedCo.contents.get(fieldID);
                fieldID=flippedCompanyModel.get(CompanyLogicRoles.COMPANY__INDUSTRY.name());
                co.industry = relatedCo.contents.get(fieldID);
                fieldID=flippedCompanyModel.get(CompanyLogicRoles.COMPANY__SIZE.name());
                co.employeeRange = relatedCo.contents.get(fieldID);
                fieldID=flippedCompanyModel.get(CompanyLogicRoles.COMPANY__SCENARIO.name());
                co.scenario = relatedCo.contents.get(fieldID);

            }
            if ( c.company==null || !c.company.objectID.getBBobjectID().equals(co.objectID.getBBobjectID())) {
                c.company = co;
                companyRepo.persist(co);
            }
        }

        c.name = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__NAME);
        c.surname = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__SURNAME);
        c.jobTitle = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__LINKEDIN_JOB_TITLE);
        c.phoneNumber = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__PHONE);
        c.email = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__EMAIL);
        c.linkedIn = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__LINKEDIN_URL);
        c.icp = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__ICP);

        SalesUser su;

        BBObjectID suid = new BBObjectID();
        suid.setTenantID(data.accountId);
        // si está vacio que coja el de la company.
        String assignToId = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__ASSIGNED_TO);
        if (assignToId !=null && !assignToId.equals("")) {
            suid.setBBobjectID(assignToId);
        } else {
            for (RawObject relatedBobject : data.relatedBobjects) {
                if(relatedBobject.isCompany()) {
                    Map<String,String> model= flipHashMap(data.frozenModel.company.fieldsModel);
                    String fieldID=model.get(CompanyLogicRoles.COMPANY__ASSIGNED_TO.name());
                    if(fieldID!=null){
                        suid.setBBobjectID(relatedBobject.contents.get(fieldID));
                        break;
                    }
                }
            }
        }

        if(suid.getBBobjectID()==null || c.assignTo==null || !c.assignTo.objectID.getBBobjectID().equals(suid.getBBobjectID())) {
            su = salesUserRepo.findById(suid);
            if (su == null) {
                su = new SalesUser();
                su.objectID = suid;
                salesUserRepo.persist(su);
            }
            c.assignTo = su;
        }


        String statusPicklistID = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__STATUS);
        if (statusPicklistID != null) {
            String statusPicklist = findPicklist(data, statusPicklistID);
            c.status=setStatusFromLogicRole(statusPicklist);
        } else c.status = Contact.STATUS_OTHER;
        if(c.status==0) c.statusPicklistID=statusPicklistID;


        if(c.attributes==null) c.attributes= new HashMap<>();
        Map<String, ExtendedAttribute> attributes=c.attributes;
        data.afterBobject.contents.forEach((k, v) -> addAttribute(attributes, data, k, v));

        persist(c);
        return c;
    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, KMesg data, String k, String v) {

        if(v==null) return; // bug panache

        String logicRole = data.frozenModel.lead.fieldsModel.get(k);
        ContactLogicRoles lrole= ContactLogicRoles.NONE;
        if (logicRole != null && !logicRole.equals(""))
            lrole = ContactLogicRoles.valueOf(logicRole);

        switch (lrole) {
            case LEAD__NAME:
            case LEAD__SURNAME:
            case LEAD__LINKEDIN_JOB_TITLE:
            case LEAD__PHONE:
            case LEAD__EMAIL:
            case LEAD__LINKEDIN_URL:
            case LEAD__ASSIGNED_TO:
            case LEAD__STATUS:
                break;
            default:
                ExtendedAttribute attribute = new ExtendedAttribute();
                attribute.assign(lrole, v);
                attributes.put(k, attribute);
                break;
        }


    }
    public int setStatusFromLogicRole(String statusPicklist) {
        int result=Contact.STATUS_OTHER;

        switch (statusPicklist) {
            case "LEAD__STATUS__NEW" -> result= Contact.STATUS_NEW;
            case "LEAD__STATUS__DELIVERED" -> result=  Contact.STATUS_DELIVERED;
            case "LEAD__STATUS__ON_PROSPECTION" ->result=  Contact.STATUS_ON_PROSPECTION;
            case "LEAD__STATUS__CONTACTED" -> result=  Contact.STATUS_CONTACTED;
            case "LEAD__STATUS__ENGAGED" ->result= Contact.STATUS_ENGAGED;
            case "LEAD__STATUS__MEETING" -> result=  Contact.STATUS_MEETING;
            case "LEAD__STATUS__NURTURING" -> result=  Contact.STATUS_NURTURING;
            case "LEAD__STATUS__DISCARDED" -> result=   Contact.STATUS_DISCARDED;
            case "LEAD__STATUS__CONTACT" -> result=   Contact.STATUS_CONTACT;
            case "LEAD__STATUS__BACKLOG" -> result=   Contact.STATUS_BACKLOG;
        }
        return result;
    }

    @Transactional
    public void removeCompany(Company c) {
        update("SET COobjectID = null where COobjectID = ?1",c.objectID.getBBobjectID());
    }
}
