package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.Company;
import com.bloobirds.datamodel.Contact;
import com.bloobirds.datamodel.SalesUser;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ContactLogicRoles;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.pipelines.messages.KMesg;
import com.bloobirds.pipelines.messages.RawObject;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import lombok.extern.java.Log;
import org.apache.camel.Body;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Log
@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<Contact, BBObjectID> {

    @Inject
    CompanyRepository companyRepo;

    @Inject
    SalesUserRepository salesUserRepo;

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
        String result = data.frozenModel.lead.picklistsModel.get(lroleID);
        return result;
    }

    @Transactional
    public Contact createContactFromKMesg(@Body KMesg data) {

        Map<String, String> flippedFieldsModel = flipHashMap(data.frozenModel.lead.fieldsModel);

        Contact c = new Contact();

        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        id.setBBobjectID(data.beforeBobject.id.objectId);
        c.objectID = id;

        Company co;
        BBObjectID coid = new BBObjectID();
        coid.setTenantID(data.accountId);
        String[] parts = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__COMPANY).split("/");
        if (parts.length == 0)
            coid.setBBobjectID("");
        else
            coid.setBBobjectID(parts[parts.length - 1]);
        co = companyRepo.findById(coid);
        if (co == null) {
            co = new Company();
            co.objectID = coid;
            companyRepo.persist(co);
        }

        c.company = co;

        c.name = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__NAME);
        c.surname = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__SURNAME);
        c.jobTitle = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__LINKEDIN_JOB_TITLE);
        c.phoneNumber = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__PHONE);
        c.email = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__EMAIL);
        c.linkedIn = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__LINKEDIN_URL);

        SalesUser su;

        BBObjectID suid = new BBObjectID();
        suid.setTenantID(data.accountId);
        // si está vacio que coja el de la company.
        String assignToId = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__ASSIGNED_TO);
        if (assignToId !=null && !assignToId.equals("")) {
            suid.setBBobjectID(assignToId);
        } else {
            assignToId ="";
            for (RawObject relatedBobject : data.relatedBobjects) {
                if(relatedBobject.isCompany()) {
                    Map<String,String> model= flipHashMap(data.frozenModel.company.fieldsModel);
                    String fieldID=model.get("COMPANY__ASSIGNED_TO");
                    if(fieldID!=null){
                        suid.setBBobjectID(relatedBobject.contents.get(fieldID));
                        log.info("assignTo:"+suid.getBBobjectID());
                        break;
                    }
                }
            }
        }

        su = salesUserRepo.findById(suid);
        if (su == null) {
            su = new SalesUser();
            su.objectID = suid;
            salesUserRepo.persist(su);
        }

        c.assignTo = su;
        String statusPicklistID = findField(data, flippedFieldsModel, ContactLogicRoles.LEAD__STATUS);
        if (statusPicklistID != null) {
            String statusPicklist = findPicklist(data, statusPicklistID);
            c.setStatusWithLRole(statusPicklist);
        } else c.status = Contact.STATUS_NO_STATUS;

        data.afterBobject.contents.forEach((k, v) -> addAttribute(c.attributes, data, k, v));

        persist(c);
        return c;
    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, KMesg data, String k, String v) {

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

}
