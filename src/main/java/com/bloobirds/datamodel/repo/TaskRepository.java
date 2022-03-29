package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.*;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.datamodel.abstraction.logicroles.CompanyLogicRoles;
import com.bloobirds.datamodel.abstraction.logicroles.TaskLogicRoles;
import com.bloobirds.pipelines.messages.Action;
import com.bloobirds.pipelines.messages.KMesg;
import com.bloobirds.pipelines.messages.RawObject;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Log
@ApplicationScoped
public class TaskRepository implements PanacheRepositoryBase<Task,BBObjectID> {

    @Inject
    CompanyRepository companyRepo;

    @Inject
    SalesUserRepository salesUserRepo;

    @Inject
    ContactRepository contactRepo;

    @Inject
    OpportunityRepository opportunityRepo;

    @Transactional
    public Task createTaskFromKMesg(@Body KMesg data) {
        Task t;
        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        id.setBBobjectID(data.afterBobject.id.objectId);
        t = findById(id);

        if (data.action.equals(Action.DELETE)) {
            if (t != null) {
                delete(t);
            }
            return t;
        }
        if (t == null) {
            t = new Task();
            t.objectID = id;
        }

        Map<String, String> flippedFieldsModel = KMesg.flipHashMap(data.frozenModel.task.fieldsModel);

        t.title = KMesg.findField(data, flippedFieldsModel, TaskLogicRoles.TASK__TITLE);

        String scheduledDate = KMesg.findField(data, flippedFieldsModel, TaskLogicRoles.TASK__SCHEDULED_DATETIME);
        try {
            LocalDateTime dateToConvert = LocalDateTime.parse(scheduledDate, DateTimeFormatter.ISO_DATE_TIME);
            t.scheduled = java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (DateTimeParseException e) { }

        String typePicklistID = KMesg.findField(data, flippedFieldsModel, TaskLogicRoles.TASK__TASK_TYPE);
        if (typePicklistID != null) {
            String typePicklist = KMesg.findPicklist(data, data.frozenModel.task.picklistsModel, typePicklistID);
            t.type = setTypeFromLogicRole(typePicklistID);
        } else t.type = Task.TASK__TYPE_NONE;
        t.typeFieldID = typePicklistID;

        String statusPicklistID = KMesg.findField(data, flippedFieldsModel, TaskLogicRoles.TASK__STATUS);
        if (statusPicklistID != null) {
            String statusPicklist = KMesg.findPicklist(data, data.frozenModel.task.picklistsModel, statusPicklistID);
            t.status = setStatusFromLogicRole(statusPicklist);
        } else t.status = Task.TASK__STATUS__NONE;
        t.statusFieldID = statusPicklistID;

        Company c;
        String[] partsc = KMesg.findField(data, flippedFieldsModel, TaskLogicRoles.TASK__COMPANY).split("/");
        if (partsc.length ==3) {
            BBObjectID coid = new BBObjectID();
            coid.setTenantID(data.accountId);
            coid.setBBobjectID(partsc[partsc.length - 1]);
            c = companyRepo.findById(coid);
            if (c == null) {
                c = new Company();
                c.objectID = coid;

                int i = 0;
                while (!data.relatedBobjects.get(i).isCompany()) i++;
                RawObject relatedCo = data.relatedBobjects.get(i);

                Map<String, String> flippedCompanyModel = KMesg.flipHashMap(data.frozenModel.company.fieldsModel);

                String fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__NAME.name());
                c.name = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__DISCARDED_REASONS.name());
                c.discardedReasons = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__NURTURING_REASONS.name());
                c.nurturingReasons = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__TARGET_MARKET.name());
                c.targetMarket = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__COUNTRY.name());
                c.country = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__INDUSTRY.name());
                c.industry = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__SIZE.name());
                c.employeeRange = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__SCENARIO.name());
                c.scenario = relatedCo.contents.get(fieldID);

            }
            if (t.company == null || !t.company.objectID.getBBobjectID().equals(c.objectID.getBBobjectID())) {
                t.company = c;
                companyRepo.persist(c);
            }
        }

        Contact co = null;
        id = new BBObjectID();
        id.setTenantID(data.accountId);
        String[] partsco = KMesg.findField(data, flippedFieldsModel, TaskLogicRoles.TASK__LEAD).split("/");

        if (partsco.length ==3) {
            String coFieldID = partsco[partsco.length - 1];
            id.setBBobjectID(coFieldID);
            co = contactRepo.findById(id);
            if (co == null) {
                co = new Contact();
                co.objectID = id;
                contactRepo.persist(co);
            }
            if (t.lead == null || !t.lead.objectID.getBBobjectID().equals(co.objectID.getBBobjectID())) {
                if(co!=null) {
                    t.lead = co;
                    contactRepo.persist(co);
                }
            }
        }

        SalesUser su;
        BBObjectID suid = new BBObjectID();
        suid.setTenantID(data.accountId);
        String assignToId = KMesg.findField(data, flippedFieldsModel, TaskLogicRoles.TASK__ASSIGNED_TO);
        if (assignToId != null && !assignToId.equals("")) {
            suid.setBBobjectID(assignToId);
        } else {
            int i = 0;
            while (!data.relatedBobjects.get(i).isCompany()) i++;
            RawObject relatedCo = data.relatedBobjects.get(i);
            Map<String, String> model = KMesg.flipHashMap(data.frozenModel.company.fieldsModel);
            String fieldID = model.get(CompanyLogicRoles.COMPANY__ASSIGNED_TO.name());
            if (fieldID != null) {
                suid.setBBobjectID(relatedCo.contents.get(fieldID));
            }
        }
        if (suid.getBBobjectID() == null || t.assignTo == null || !t.assignTo.objectID.getBBobjectID().equals(suid.getBBobjectID())) {
            su = salesUserRepo.findById(suid);
            if (su == null) {
                su = new SalesUser();
                su.objectID = suid;
                salesUserRepo.persist(su);
            }
            t.assignTo = su;
        }


        Opportunity o = null;


        BBObjectID idOP = new BBObjectID();
        idOP.setTenantID(data.accountId);

        String[] partso = KMesg.findField(data, flippedFieldsModel, TaskLogicRoles.TASK__OPPORTUNITY).split("/");

        if (partso.length == 3) {
            String oFieldID = partso[partso.length - 1];
            idOP.setBBobjectID(oFieldID);
            o = opportunityRepo.findById(idOP);
            if (o == null) {
                o = new Opportunity();
                o.objectID = id;
            } else { }
            if (t.opportunity == null || !t.opportunity.objectID.getBBobjectID().equals(o.objectID.getBBobjectID())) {
                if (o!=null) {
                    t.opportunity = o;
                    opportunityRepo.persist(o);
                }
            }
        }

        if (t.attributes == null) t.attributes = new HashMap<>();
        Map<String, ExtendedAttribute> attributes = t.attributes;
        data.afterBobject.contents.forEach((k, v) -> addAttribute(attributes, data, k, v));

        persist(t);
        return t;
    }

    private int setTypeFromLogicRole(String typePicklistID) {
        int result= Task.TASK__TYPE_NONE;
        switch(typePicklistID){
            case "CONTACT_BEFORE_MEETING" -> result = Task.TASK__TYPE_CONTACT_BEFORE_MEETING;
            case "PROSPECT_CADENCE" -> result = Task.TASK__TYPE_PROSPECT_CADENCE;
            case "ADD_QC" -> result = Task.TASK__TYPE_ADD_QC;
            case "NEXT_STEP" -> result = Task.TASK__TYPE_NEXT_STEP;
            case "ALLOCATE_QC" -> result = Task.TASK__TYPE_ALLOCATE_QC;
            case "LEADS_TO_QC" -> result = Task.TASK__TYPE_ADD_LEADS_TO_QC;
            case "CONTACT" -> result = Task.TASK__TYPE_CONTACT;
            case "START_CADENCE" -> result = Task.TASK__TYPE_START_CADENCE;
            case "MEETING" -> result = Task.TASK__TYPE_MEETING;
            case "SCHEDULED_EMAIL" -> result = Task.TASK__TYPE_SCHEDULED_EMAIL;
        }
        return result;
    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, KMesg data, String k, String v) {
        if (v == null) return; // bug panache

        String logicRole = data.frozenModel.task.fieldsModel.get(k);
        TaskLogicRoles lrole = TaskLogicRoles.NONE;
        if (logicRole != null && !logicRole.equals(""))
            lrole = TaskLogicRoles.valueOf(logicRole);

        switch (lrole) {
            case TASK__ASSIGNED_TO:
            case TASK__SCHEDULED_DATETIME:
            case TASK__COMPANY:
            case TASK__TITLE:
            case TASK__STATUS:
            case TASK__TASK_TYPE:
                break;
            default:
                ExtendedAttribute attribute = new ExtendedAttribute();
                attribute.assign(lrole, v);
                attributes.put(k, attribute);
                break;
        }
    }

    private int setStatusFromLogicRole(String statusPicklist) {
        int result= Task.TASK__STATUS__NONE;

        switch (statusPicklist){
            case "TASK__STATUS__REJECTED" -> result = Task.TASK__STATUS__REJECTED;
            case "TASK__STATUS__COMPLETED_OVERDUE" -> result = Task.TASK__STATUS__COMPLETED_OVERDUE;
            case "TASK__STATUS__COMPLETED" -> result = Task.TASK__STATUS__COMPLETED;
            case "TASK__STATUS__TODO" -> result = Task.TASK__STATUS__TODO;
            case "TASK__STATUS__OVERDUE" -> result = Task.TASK__STATUS__OVERDUE;
        }

        return result;
    }
}
