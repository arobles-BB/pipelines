package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.Company;
import com.bloobirds.datamodel.SalesUser;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.datamodel.abstraction.logicroles.CompanyLogicRoles;
import com.bloobirds.pipelines.messages.Action;
import com.bloobirds.pipelines.messages.KMesg;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import lombok.extern.java.Log;
import org.apache.camel.Body;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log
@ApplicationScoped
public class CompanyRepository implements PanacheRepositoryBase<Company, BBObjectID> {

    @Inject
    ActivityRepository activityRepo;

    @Inject
    ContactRepository contactRepo;

    @Inject
    OpportunityRepository opportunityRepo;

    @Inject
    TaskRepository taskRepo;

    @Inject
    SalesUserRepository userRepo;

    public static int setStatusFromLogicRole(String statusPicklist) {

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

    @Transactional
    public Company newCompanyFromKMsg(@Body KMesg data) {
        Map<String, String> flippedFieldsModel = KMesg.flipHashMap(data.frozenModel.company.fieldsModel);

        Company c;

        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        id.setBBobjectID(data.afterBobject.id.objectId);

        c = findById(id);
        if (data.action.equals(Action.DELETE)) {
            if (c != null) {
                taskRepo.deleteByCompany(c);
                opportunityRepo.deleteByCompany(c);
                activityRepo.deleteByCompany(c);
                contactRepo.removeCompany(c);
                delete(c);
            }
            return c;
        }
        if (c == null) {
            c = new Company();
            c.objectID = id;
        }


        c.name = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__NAME);

        SalesUser su;

        BBObjectID suid = new BBObjectID();
        suid.setTenantID(data.accountId);

        String assignToId = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__ASSIGNED_TO);
        suid.setBBobjectID(assignToId);

        su = userRepo.findById(suid);
        if (su == null) {
            su = new SalesUser();
            su.objectID = suid;
            userRepo.persist(su);
        }

        if (c.assignTo == null || !c.assignTo.objectID.getBBobjectID().equals(su.objectID.getBBobjectID()))
            c.assignTo = su;

        String statusPicklistID = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__STATUS);

        if (statusPicklistID != null) {
            String status = data.frozenModel.company.picklistsModel.get(statusPicklistID);


            int newStatus = setStatusFromLogicRole(status);
            if (newStatus != c.status) {
                c.prevStatus = c.status;
                c.status = newStatus;
            }
        } else c.status = Company.COMPANY__STATUS__OTHER;

        if (statusPicklistID != null && !statusPicklistID.equals(c.statusFieldID)) {
            c.dateStatusUpdate = getUpdateDate(data, flippedFieldsModel);
            c.statusFieldID = statusPicklistID;
        }

        c.sourcePicklistID = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__SOURCE);
        c.source = setSourceFromLogicRole(c.sourcePicklistID);

        String field = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__STATUS__CHANGED_DATE_READY_TO_PROSPECT);
        if (field != null) {
            try {
                LocalDateTime d = LocalDateTime.parse(field, DateTimeFormatter.ISO_DATE_TIME);
                c.startedToProspect = java.util.Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException e) {
            }
        }
        c.discardedReasons = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__DISCARDED_REASONS);
        c.nurturingReasons = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__NURTURING_REASONS);
        c.cadence = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__CADENCE);
        c.targetMarket = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__TARGET_MARKET);
        c.country = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__COUNTRY);
        c.industry = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__INDUSTRY);
        c.employeeRange = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__SIZE);
        c.scenario = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__SCENARIO);


        if (c.attributes == null) c.attributes = new HashMap<>();
        Map<String, ExtendedAttribute> attributes = c.attributes;
        data.afterBobject.contents.forEach((k, v) -> addAttribute(attributes, data, k, v));

        //        c.vertical; // ??


        persist(c);
        return c;
    }

    private Date getUpdateDate(KMesg data, Map<String, String> flippedFieldsModel) {
        String date = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__UPDATE_DATETIME);
        if (date == null)
            date = KMesg.findField(data, flippedFieldsModel, CompanyLogicRoles.COMPANY__CREATION_DATETIME);
        if (date == null) return new Date();

        Date dateValue = null;

        try {
            LocalDateTime dateToConvert = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
            dateValue = java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (DateTimeParseException e) {
        }
        return dateValue;
    }

    private int setSourceFromLogicRole(String sourcePicklistID) {
        int result = Company.COMPANY__SOURCE__OTHER;
        switch (sourcePicklistID) {
            case "COMPANY__SOURCE__OUTBOUND" -> result = Company.COMPANY__SOURCE__OUTBOUND;
            case "COMPANY__SOURCE__INBOUND" -> result = Company.COMPANY__SOURCE__INBOUND;
        }
        return result;
    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, KMesg data, String k, String v) {
        if (v == null) {
            attributes.remove(k);
            return; // BUG Pnache! no podemos guardar los null o el persist no hace update y da duplicate key
        }

        ExtendedAttribute result = null;

        String logicRole = data.frozenModel.company.fieldsModel.get(k);
        CompanyLogicRoles lrole;

        switch (logicRole) {
            case "COMPANY__NAME":
            case "COMPANY__STATUS__CHANGED_DATE_READY_TO_PROSPECT":
            case "COMPANY__DISCARDED_REASONS":
            case "COMPANY__NURTURING_REASONS":
            case "COMPANY__SOURCE":
            case "COMPANY__TARGET_MARKET":
            case "COMPANY__ASSIGNED_TO":
            case "COMPANY__STATUS":
            case "COMPANY__COUNTRY":
            case "COMPANY__INDUSTRY":
            case "COMPANY__SIZE":
            case "COMPANY__SCENARIO":
            case "COMPANY__CADENCE":
                break;
            default:
                ExtendedAttribute attribute = new ExtendedAttribute();
                if (logicRole != null && !logicRole.equals("")) {
                    try {
                        lrole = CompanyLogicRoles.valueOf(logicRole);
                        attribute.assign(lrole, v);
                        attributes.put(k, attribute);
                        result = attribute;
                    } catch (Exception e) {
                    }
                }
                break;
        }
    }

}
