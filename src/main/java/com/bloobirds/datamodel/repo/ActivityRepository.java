package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.*;
import com.bloobirds.datamodel.abstraction.Activity;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.logicroles.ActivityLogicRoles;
import com.bloobirds.datamodel.abstraction.logicroles.CompanyLogicRoles;
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
import java.util.Date;
import java.util.Map;

@Log
@ApplicationScoped
public class ActivityRepository implements PanacheRepositoryBase<Activity, BBObjectID> {
    @Inject
    CompanyRepository companyRepo;

    @Inject
    ContactRepository contactRepo;

    @Inject
    SalesUserRepository salesUserRepo;

    @Inject
    ActivityCallRepository callRepo;

    @Transactional
    public Activity newActivityFromKMsg(@Body KMesg data) {
        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        id.setBBobjectID(data.afterBobject.id.objectId);
        Activity a = findById(id);
        if (data.action.equals(Action.DELETE)) {
            if (a != null) delete(a);
            return a;
        }
        if (a == null) a = newActivity(id, data);
        else updateActivity(a, data);
        // no persistimos activity si llegamos a este punto sin guardar es por que no es d eun tipo conocido
        return a;
    }

    private void updateActivity(Activity a, KMesg data) {
        Map<String, String> flippedFieldsModel = KMesg.flipHashMap(data.frozenModel.activity.fieldsModel);
        a.date = getActivityDate(data, flippedFieldsModel);
        getChannel(a, data, flippedFieldsModel);

        Company co = getCompany(data, flippedFieldsModel);
        Contact le = getLead(data, flippedFieldsModel);
        SalesUser su = getUser(data, flippedFieldsModel);
        if (a.company != null && co != null && !co.objectID.getBBobjectID().equals(a.company.objectID.getBBobjectID())) {
            a.company = co;
            a.targetMarket = a.company.targetMarket;
            a.scenario = a.company.scenario;
        }
        if (a.lead != null && le != null && !le.objectID.getBBobjectID().equals(a.lead.objectID.getBBobjectID()))
            a.lead = le;
        if (a.user != null && su != null && !su.objectID.getBBobjectID().equals(a.user.objectID.getBBobjectID())) {
            a.user = su;
            a.icp = a.lead.icp;
        }
        switch (a.getActivityType()) {
            case Activity.ACTIVITY__TYPE__CALL -> callRepo.newAndUpdate((ActivityCall) a, data, flippedFieldsModel);
        }
    }

    private Activity newActivity(BBObjectID id, KMesg data) {
        Activity a = null;

        Map<String, String> flippedFieldsModel = KMesg.flipHashMap(data.frozenModel.activity.fieldsModel);

        String activityTypeId = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__TYPE);// ACTIVITY__TYPE
        if (activityTypeId != null) {
            String picklistID = data.frozenModel.activity.picklistsModel.get(activityTypeId);
            if (picklistID != null) {
                switch (picklistID) {
                    case "ACTIVITY__TYPE__STATUS" -> a = new ActivityStatus();
                    case "ACTIVITY__TYPE__INBOUND" -> a = new ActivityInbound();
                    case "ACTIVITY__TYPE__NOTE" -> a = new ActivityNote();
                    case "ACTIVITY__TYPE__MEETING" -> a = new ActivityMeeting();
                    case "ACTIVITY__TYPE__LINKEDIN_MESSAGE" -> a = new ActivityLinkedIn();
                    case "ACTIVITY__TYPE__EMAIL" -> a = new ActivityEmail();
                    case "ACTIVITY__TYPE__CADENCE" -> a = new ActivityCadence();
                    case "ACTIVITY__TYPE__CALL" -> a = new ActivityCall();
                    default -> a = new Activity() {
                        @Override
                        public int getActivityType() {
                            return -1;
                        }
                    };
                }
                a.objectID = id;
                a.date = getActivityDate(data, flippedFieldsModel);
                getChannel(a, data, flippedFieldsModel);

                a.company = getCompany(data, flippedFieldsModel);
                a.lead = getLead(data, flippedFieldsModel);
                a.user = getUser(data, flippedFieldsModel);

                if (a.company != null) {
                    a.targetMarket = a.company.targetMarket;
                    a.scenario = a.company.scenario;
                }
                if (a.lead != null) a.icp = a.lead.icp;

                switch (a.getActivityType()) {
                    case Activity.ACTIVITY__TYPE__CALL -> callRepo.newAndUpdate((ActivityCall) a, data, flippedFieldsModel);
                }
            }
        }
        return a;
    }

    private SalesUser getUser(KMesg data, Map<String, String> flippedFieldsModel) {
        SalesUser su = null;
        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        String suID = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__USER);
        if (suID != null) {
            id.setBBobjectID(suID);
            su = salesUserRepo.findById(id);
            if (su == null) {
                su = new SalesUser();
                su.objectID = id;
                salesUserRepo.persist(su);
            }
        }
        return su;
    }

    private Contact getLead(KMesg data, Map<String, String> flippedFieldsModel) {
        Contact co = null;
        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        String[] parts = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__LEAD).split("/");
        if (parts.length != 0) {
            String coFieldID = parts[parts.length - 1];
            id.setBBobjectID(coFieldID);
            co = contactRepo.findById(id);
            if (co == null) {
                co = new Contact();
                co.objectID = id;
                contactRepo.persist(co);
            }
        }
        return co;
    }

    private Company getCompany(KMesg data, Map<String, String> flippedFieldsModel) {
        Company co = null;
        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        String[] parts = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__COMPANY).split("/");
        if (parts.length != 0) {
            String coFieldID = parts[parts.length - 1];
            id.setBBobjectID(coFieldID);
            co = companyRepo.findById(id);
            if (co == null) {
                co = new Company();
                co.objectID = id;
                int i = 0;
                while (!data.relatedBobjects.get(i).isCompany()) i++;
                RawObject relatedCo = data.relatedBobjects.get(i);

                Map<String, String> flippedCompanyModel = KMesg.flipHashMap(data.frozenModel.company.fieldsModel);

                String fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__NAME.name());
                co.name = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__DISCARDED_REASONS.name());
                co.discardedReasons = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__NURTURING_REASONS.name());
                co.nurturingReasons = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__TARGET_MARKET.name());
                co.targetMarket = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__COUNTRY.name());
                co.country = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__INDUSTRY.name());
                co.industry = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__SIZE.name());
                co.employeeRange = relatedCo.contents.get(fieldID);
                fieldID = flippedCompanyModel.get(CompanyLogicRoles.COMPANY__SCENARIO.name());
                co.scenario = relatedCo.contents.get(fieldID);

                companyRepo.persist(co);
            }
        }
        return co;
    }

    private void getChannel(Activity a, KMesg data, Map<String, String> flippedFieldsModel) {
        a.channelID = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__CHANNEL);

        if (a.channelID != null) {
            String picklistID = data.afterBobject.contents.get(a.channelID);
            if (picklistID != null) {
                String channel = data.frozenModel.activity.picklistsModel.get(picklistID);
                a.channelID = picklistID;
                switch (channel) {
                    case "ACTIVITY__CHANNEL__EMAIL" -> a.channel = Activity.ACTIVITY__CHANNEL__EMAIL;
                    case "ACTIVITY__CHANNEL__LINKEDIN_MESSAGE" -> a.channel = Activity.ACTIVITY__CHANNEL__LINKEDIN_MESSAGE;
                    case "ACTIVITY__CHANNEL__CALL" -> a.channel = Activity.ACTIVITY__CHANNEL__CALL;
                    default -> a.channel = Activity.ACTIVITY__CHANNEL__OTHER;
                }
            }
        }
    }

    private Date getActivityDate(KMesg data, Map<String, String> flippedFieldsModel) {
        // ACTIVITY__TIME vs ACTIVITY__CREATION_DATETIME vs. ACTIVITY__UPDATE_DATETIME
        String date = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__TIME);
        if (date == null)
            date = KMesg.findField(data, flippedFieldsModel, ActivityLogicRoles.ACTIVITY__CREATION_DATETIME);
        if (date == null) return null;

        Date dateValue = null;

        try {
            LocalDateTime dateToConvert = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
            dateValue = java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (DateTimeParseException e) {
        }
        return dateValue;
    }

    @Transactional
    public void deleteByContact(Contact c) {
        delete("lead", c);
    }

    @Transactional
    public void deleteByCompany(Company c) {
        delete("company", c);
    }
}
