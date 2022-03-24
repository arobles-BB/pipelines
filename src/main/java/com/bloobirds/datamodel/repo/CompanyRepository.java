package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.Company;
import com.bloobirds.datamodel.SalesUser;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.CompanyLogicRoles;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.pipelines.messages.KMesg;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.apache.camel.Body;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CompanyRepository implements PanacheRepositoryBase<Company, BBObjectID> {

    private static Map<String, String> flipHashMap(Map<String, String> map) {
        Map<String, String> result = new HashMap<>();
        map.forEach((k, v) -> result.put(v, k));
        return result;
    }

    private static String findField(KMesg data, Map<String, String> flippedFieldsModel, CompanyLogicRoles lrole) {
        String result = "";
        String fID = flippedFieldsModel.get(lrole.name());
        if (fID != null) result = data.afterBobject.contents.get(fID);
        return result;
    }

    @Transactional
    public Company newCompanyFromKMsg(@Body KMesg data) {
        Map<String, String> flippedFieldsModel = flipHashMap(data.frozenModel.company.fieldsModel);

        Company c = new Company();

        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        id.setBBobjectID(data.afterBobject.id.objectId);
        c.objectID = id;

        c.name = findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__NAME);

        SalesUser su;

        BBObjectID suid = new BBObjectID();
        suid.setTenantID(data.accountId);

        String assignToId = findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__ASSIGNED_TO);
        suid.setBBobjectID(assignToId);

        c.statusPicklistID = findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__STATUS);
        String field = data.frozenModel.company.picklistsModel.get(c.statusPicklistID);
        c.status = setStatusFromLogicRole(field);

        c.sourcePicklistID= findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__SOURCE);
        c.source=setSourceFromLogicRole(c.sourcePicklistID);

//        c.startedToProspect; //COMPANY__STATUS__CHANGED_DATE_READY_TO_PROSPECT
//
//        c.discardedReasons; // COMPANY__DISCARDED_REASONS
//        c.nurturingReasons; // COMPANY__NURTURING_REASONS
//

//
//        c.targetMarket; // COMPANY__TARGET_MARKET
//        c.country; // COMPANY__COUNTRY
//        c.industry; // COMPANY__INDUSTRY
//        c.vertical; // ??
//        c.employeeRange; // COMPANY__SIZE
//        c.scenario; // COMPANY__SCENARIO

        persist(c);
        return c;
    }

    private int setSourceFromLogicRole(String sourcePicklistID) {
        int result = Company.COMPANY__SOURCE__OTHER;
        switch (sourcePicklistID){
            case "COMPANY__SOURCE__OUTBOUND" -> result= Company.COMPANY__SOURCE__OUTBOUND;
            case "COMPANY__SOURCE__INBOUND" -> result= Company.COMPANY__SOURCE__INBOUND;
        }
        return result;
    }

    private int setStatusFromLogicRole(String statusPicklist) {

        int result = Company.COMPANY__STATUS__OTHER;
        switch (statusPicklist) {
            case "COMPANY__STATUS__NEW" -> result = Company.COMPANY__STATUS__NEW;
            case "COMPANY__STATUS__DELIVERED" -> result = Company.COMPANY__STATUS__DELIVERED;
            case "COMPANY__STATUS__CONTACTED" -> result = Company.COMPANY__STATUS__CONTACTED;
            case "COMPANY__STATUS__ENGAGED" -> result = Company.COMPANY__STATUS__ENGAGED;
            case "COMPANY__STATUS__MEETING" -> result = Company.COMPANY__STATUS__MEETING;
            case "COMPANY__STATUS__CLIENT" -> result = Company.COMPANY__STATUS__CLIENT;
            case "COMPANY__STATUS__NURTURING" -> result = Company.COMPANY__STATUS__NURTURING;
            case "COMPANY__STATUS__DISCARDED" -> result = Company.COMPANY__STATUS__DISCARDED;
            case "COMPANY__STATUS__ACCOUNT" -> result = Company.COMPANY__STATUS__ACCOUNT;
            case "COMPANY__STATUS__BACKLOG" -> result = Company.COMPANY__STATUS__BACKLOG;
            case "COMPANY__STATUS__ON_PROSPECTION" -> result = Company.COMPANY__STATUS__ON_PROSPECTION;
            case "COMPANY__STATUS__READY_TO_PROSPECT" -> result = Company.COMPANY__STATUS__READY_TO_PROSPECT;
            case "COMPANY__STATUS__FINDING_LEADS" -> result = Company.COMPANY__STATUS__FINDING_LEADS;
        }
        return result;
    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, String logicRole, String k, String v) {

        CompanyLogicRoles lrole = CompanyLogicRoles.NONE;
        if (logicRole != null && !logicRole.equals(""))
            lrole = CompanyLogicRoles.valueOf(logicRole);

        switch (lrole) {
            case COMPANY__NAME:
            case COMPANY__STATUS__CHANGED_DATE_READY_TO_PROSPECT:
            case COMPANY__DISCARDED_REASONS:
            case COMPANY__NURTURING_REASONS:
            case COMPANY__SOURCE:
            case COMPANY__TARGET_MARKET:
            case COMPANY__ASSIGNED_TO:
            case COMPANY__STATUS:
            case COMPANY__COUNTRY:
            case COMPANY__INDUSTRY:
            case COMPANY__SIZE:
            case COMPANY__SCENARIO:
                break;
            default:
                ExtendedAttribute attribute = new ExtendedAttribute();
                attribute.assign(lrole, v);
                attributes.put(k, attribute);
                break;
        }

    }

}
