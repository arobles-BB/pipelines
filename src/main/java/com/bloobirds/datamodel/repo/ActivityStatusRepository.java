package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.ActivityStatus;
import com.bloobirds.datamodel.Company;
import com.bloobirds.datamodel.Contact;
import com.bloobirds.datamodel.Opportunity;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.datamodel.abstraction.logicroles.ActivityLogicRoles;
import com.bloobirds.datamodel.abstraction.logicroles.ActivityStatusLogicRoles;
import com.bloobirds.pipelines.messages.KMesg;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import lombok.extern.java.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Log
@ApplicationScoped
public class ActivityStatusRepository implements PanacheRepositoryBase<ActivityStatus, BBObjectID> {

    @Inject
    CompanyRepository companyRepo;

    @Inject
    ContactRepository contactRepo;

    @Inject
    OpportunityRepository opportunityRepo;

    @Transactional
    public void newAndUpdate(ActivityStatus a, KMesg data, Map<String, String> flippedFieldsModel) {
        a.title = KMesg.findField(data, flippedFieldsModel, ActivityStatusLogicRoles.ACTIVITY__STATUS_TITLE);

        a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__NONE;
        a.statusFieldID = KMesg.findField(data, flippedFieldsModel, ActivityStatusLogicRoles.ACTIVITY__TYPE_STATUS);
        if (a.statusFieldID != null) {
            String status = data.frozenModel.activity.picklistsModel.get(a.statusFieldID);
            switch (status) {
                case "ACTIVITY__TYPE_STATUS__LEAD_STATUS_CHANGED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__LEAD_STATUS_CHANGED;
                case "ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED;
                case "ACTIVITY__TYPE_STATUS__NEW_LEAD_CREATED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__NEW_LEAD_CREATED;
                case "ACTIVITY__TYPE_STATUS__COMPANY_STATUS_CHANGED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__COMPANY_STATUS_CHANGED;
                case "ACTIVITY__TYPE_STATUS__COMPANY_ASSIGNED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__COMPANY_ASSIGNED;
                case "ACTIVITY__TYPE_STATUS__COMPANY_CREATED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__COMPANY_CREATED;
                case "ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED_TO_OPPORTUNITY" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED_TO_OPPORTUNITY;
                case "ACTIVITY__TYPE_STATUS__OPPORTUNITY_CREATED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__OPPORTUNITY_CREATED;
                case "ACTIVITY__TYPE_STATUS__OPPORTUNITY_ASSIGNED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__OPPORTUNITY_ASSIGNED;
                case "ACTIVITY__TYPE_STATUS__OPPORTUNITY_STATUS_CHANGED" -> a.status = ActivityStatus.ACTIVITY__TYPE_STATUS__OPPORTUNITY_STATUS_CHANGED;
            }
        }
        String automated = KMesg.findField(data, flippedFieldsModel, ActivityStatusLogicRoles.ACTIVITY__DATA_SOURCE_AUTOMATED);
        a.automated = false;
        switch (automated) {
            case "DATA_SOURCE_AUTOMATED__YES" -> a.automated = true;
            case "DATA_SOURCE_AUTOMATED__NO" -> a.automated = false;
        }

        a.dataSource = ActivityStatus.DATA_SOURCE__NONE;
        a.dataSourceID = KMesg.findField(data, flippedFieldsModel, ActivityStatusLogicRoles.ACTIVITY__DATA_SOURCE);
        if (a.statusFieldID != null) {
            String datasource = data.frozenModel.activity.picklistsModel.get(a.statusFieldID);
            switch (datasource) {
                case "DATA_SOURCE__LINKEDIN_CHROME_EXTENSION" -> a.dataSource = ActivityStatus.DATA_SOURCE__LINKEDIN_CHROME_EXTENSION;
                case "DATA_SOURCE__FUTURE_ACTIONS" -> a.dataSource = ActivityStatus.DATA_SOURCE__FUTURE_ACTIONS;
                case "DATA_SOURCE__WORKFLOW" -> a.dataSource = ActivityStatus.DATA_SOURCE__WORKFLOW;
                case "DATA_SOURCE__PUBLIC_API" -> a.dataSource = ActivityStatus.DATA_SOURCE__PUBLIC_API;
                case "DATA_SOURCE__WEB_ADMIN" -> a.dataSource = ActivityStatus.DATA_SOURCE__WEB_ADMIN;
                case "DATA_SOURCE__HUBSPOT" -> a.dataSource = ActivityStatus.DATA_SOURCE__HUBSPOT;
                case "DATA_SOURCE__IMPORT" -> a.dataSource = ActivityStatus.DATA_SOURCE__IMPORT;
                case "DATA_SOURCE__SALESFORCE" -> a.dataSource = ActivityStatus.DATA_SOURCE__SALESFORCE;
                case "DATA_SOURCE__WEB_APP" -> a.dataSource = ActivityStatus.DATA_SOURCE__WEB_APP;
                case "DATA_SOURCE__NYLAS" -> a.dataSource = ActivityStatus.DATA_SOURCE__NYLAS;
                case "DATA_SOURCE__ZAPIER" -> a.dataSource = ActivityStatus.DATA_SOURCE__ZAPIER;
                case "DATA_SOURCE__SUPPORT_PANEL" -> a.dataSource = ActivityStatus.DATA_SOURCE__SUPPORT_PANEL;
                case "DATA_SOURCE__TWILIO" -> a.dataSource = ActivityStatus.DATA_SOURCE__TWILIO;
                case "DATA_SOURCE__AIRCALL" -> a.dataSource = ActivityStatus.DATA_SOURCE__AIRCALL;
                case "DATA_SOURCE__CALLS_SYNC_APP" -> a.dataSource = ActivityStatus.DATA_SOURCE__CALLS_SYNC_APP;
            }
        }

        a.changedFrom = KMesg.findField(data, flippedFieldsModel, ActivityStatusLogicRoles.ACTIVITY__TYPE_STATUS_CHANGED_FROM);
        a.changedTo = KMesg.findField(data, flippedFieldsModel, ActivityStatusLogicRoles.ACTIVITY__TYPE_STATUS_CHANGED_TO);

        if (a.attributes == null) a.attributes = new HashMap<>();
        data.afterBobject.contents.forEach((k, v) -> addAttribute(a.attributes, data, k, v));

        persist(a);

        switch (a.status) {
            case ActivityStatus.ACTIVITY__TYPE_STATUS__LEAD_STATUS_CHANGED,
                    ActivityStatus.ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED,
                    ActivityStatus.ACTIVITY__TYPE_STATUS__NEW_LEAD_CREATED -> chekLead(a, flippedFieldsModel, data);
            case ActivityStatus.ACTIVITY__TYPE_STATUS__COMPANY_STATUS_CHANGED,
                    ActivityStatus.ACTIVITY__TYPE_STATUS__COMPANY_ASSIGNED,
                    ActivityStatus.ACTIVITY__TYPE_STATUS__COMPANY_CREATED -> chekCompany(a, flippedFieldsModel, data);
            case ActivityStatus.ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED_TO_OPPORTUNITY,
                    ActivityStatus.ACTIVITY__TYPE_STATUS__OPPORTUNITY_CREATED,
                    ActivityStatus.ACTIVITY__TYPE_STATUS__OPPORTUNITY_ASSIGNED,
                    ActivityStatus.ACTIVITY__TYPE_STATUS__OPPORTUNITY_STATUS_CHANGED -> chekOpportunity(a, flippedFieldsModel, data);
        }

    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, KMesg data, String k, String v) {

        if (ActivityRepository.addAttribute(attributes, data, k, v) != null) return;
        if (v == null) {
            attributes.remove(k);
            return; // BUG Pnache! no podemos guardar los null o el persist no hace update y da duplicate key
        }
        String logicRole = data.frozenModel.activity.fieldsModel.get(k);
        ActivityStatusLogicRoles lrole;

        switch (logicRole) {
            case "ACTIVITY__DATA_SOURCE_AUTOMATED":
            case "ACTIVITY__DATA_SOURCE":
            case "ACTIVITY__STATUS_TITLE":
            case "ACTIVITY__TYPE_STATUS_CHANGED_FROM":
            case "ACTIVITY__TYPE_STATUS_CHANGED_TO":
            case "ACTIVITY__TYPE_STATUS":
                break;
            default:
                ExtendedAttribute attribute = new ExtendedAttribute();
                if (logicRole != null && !logicRole.equals("")) {
                    try {
                        lrole = ActivityStatusLogicRoles.valueOf(logicRole);
                        attribute.assign(lrole, v);
                    } catch (Exception e) {
                        log.info("Invalid Logic Role:" + e.getMessage());
                    }
                }
                attributes.put(k, attribute);
                break;
        }
    }

    private void chekOpportunity(ActivityStatus a, Map<String, String> flippedFieldsModel, KMesg data) {
        Opportunity o = null;
        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        String oID = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__OPPORTUNITY);
        if (oID != null) {
            id.setBBobjectID(oID);
            o = opportunityRepo.findById(id);
            if (o == null) {
                o = new Opportunity();
                o.objectID = id;
                opportunityRepo.persist(o);
            }
        }
        a.opportunity = o;

        if (!o.statusFieldID.equals(a.changedTo)) {
            o.prevStatus=o.status;
            o.statusFieldID = a.changedTo;
            String statusLogicRole = data.frozenModel.opportunity.fieldsModel.get(a.changedTo);
            o.status = OpportunityRepository.setStatusFromLogicRole(statusLogicRole);
            o.dateStatusUpdate = ActivityRepository.getActivityDate(data, flippedFieldsModel);
            opportunityRepo.persist(o);
        }
    }

    private void chekCompany(ActivityStatus a, Map<String, String> flippedFieldsModel, KMesg data) {
        Company c = null;
        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        String cID = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__COMPANY);
        if (cID != null) {
            id.setBBobjectID(cID);
            c = companyRepo.findById(id);
            if (c == null) {
                c = new Company();
                c.objectID = id;
                companyRepo.persist(c);
            }
        }
        a.company = c;
        if (!c.statusFieldID.equals(a.changedTo)) {
            c.prevStatus=c.status;
            c.statusFieldID = a.changedTo;
            String statusLogicRole = data.frozenModel.company.fieldsModel.get(a.changedTo);
            c.status = CompanyRepository.setStatusFromLogicRole(statusLogicRole);
            companyRepo.persist(c);
        }
    }

    private void chekLead(ActivityStatus a, Map<String, String> flippedFieldsModel, KMesg data) {
        Contact co = null;
        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        String coID = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__LEAD);
        if (coID != null) {
            id.setBBobjectID(coID);
            co = contactRepo.findById(id);
            if (co == null) {
                co = new Contact();
                co.objectID = id;
                contactRepo.persist(co);
            }
        }
        a.lead = co;
        if (!co.statusFieldID.equals(a.changedTo)) {
            co.prevStatus=co.status;
            co.statusFieldID = a.changedTo;
            String statusLogicRole = data.frozenModel.lead.fieldsModel.get(a.changedTo);
            co.status = ContactRepository.setStatusFromLogicRole(statusLogicRole);
            contactRepo.persist(co);
        }
    }


}
