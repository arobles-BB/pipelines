package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.Company;
import com.bloobirds.datamodel.Opportunity;
import com.bloobirds.datamodel.SalesUser;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.datamodel.abstraction.logicroles.CompanyLogicRoles;
import com.bloobirds.datamodel.abstraction.logicroles.ContactLogicRoles;
import com.bloobirds.datamodel.abstraction.logicroles.OpportunityLogicRoles;
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
import java.util.HashMap;
import java.util.Map;

@Log
@ApplicationScoped
public class OpportunityRepository implements PanacheRepositoryBase<Opportunity, BBObjectID> {
    @Inject
    CompanyRepository companyRepo;

    @Inject
    SalesUserRepository salesUserRepo;

    @Inject
    ContactRepository contactRepo;

    @Transactional
    public Opportunity createOpportunityFromKMesg(@Body KMesg data) {
        Opportunity o;

        BBObjectID id = new BBObjectID();
        id.setTenantID(data.accountId);
        id.setBBobjectID(data.afterBobject.id.objectId);
        o = findById(id);

        if (data.action.equals(Action.DELETE)) {
            if (o != null) {
                delete(o);
            }
            return o;
        }
        if (o == null) {
            o = new Opportunity();
            o.objectID = id;
        }

        Map<String, String> flippedFieldsModel = KMesg.flipHashMap(data.frozenModel.opportunity.fieldsModel);

        o.name = KMesg.findField(data, flippedFieldsModel, OpportunityLogicRoles.OPPORTUNITY__NAME);


        String amount = KMesg.findField(data, flippedFieldsModel, OpportunityLogicRoles.OPPORTUNITY__AMOUNT);
        try {
            o.amount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {  }

        String typePicklistID = KMesg.findField(data, flippedFieldsModel, OpportunityLogicRoles.OPPORTUNITY__TYPE);
        if (typePicklistID != null) {
            String typePicklist = KMesg.findPicklist(data, data.frozenModel.opportunity.picklistsModel, typePicklistID);
            o.type = setTypeFromLogicRole(typePicklistID);
        } else o.status = Opportunity.OPPORTUNITY__STATUS__NONE;
        o.typeFieldID = typePicklistID;


        String statusPicklistID = KMesg.findField(data, flippedFieldsModel, OpportunityLogicRoles.OPPORTUNITY__STATUS);
        if (statusPicklistID != null) {
            String statusPicklist = KMesg.findPicklist(data, data.frozenModel.opportunity.picklistsModel, statusPicklistID);
            o.status = setStatusFromLogicRole(statusPicklist);
        } else o.status = Opportunity.OPPORTUNITY__STATUS__NONE;
        o.statusFieldID = statusPicklistID;

        String creationDate = KMesg.findField(data, flippedFieldsModel, OpportunityLogicRoles.OPPORTUNITY__CREATION_DATETIME); //    OPPORTUNITY__CREATION_DATE?
        try {
            LocalDateTime dateToConvert = LocalDateTime.parse(creationDate, DateTimeFormatter.ISO_DATE_TIME);
            o.creationDate = java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (DateTimeParseException e) { }

        String closingDate = KMesg.findField(data, flippedFieldsModel, OpportunityLogicRoles.OPPORTUNITY__CLOSE_DATE); //    OPPORTUNITY__CLOSE_DATE,
        try {
            LocalDateTime dateToConvert = LocalDateTime.parse(closingDate, DateTimeFormatter.ISO_DATE_TIME);
            o.closingDate = java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (DateTimeParseException e) { }


        Company co;
        String[] parts = KMesg.findField(data, flippedFieldsModel, OpportunityLogicRoles.OPPORTUNITY__COMPANY).split("/");
        if (parts.length != 0) {
            BBObjectID coid = new BBObjectID();
            coid.setTenantID(data.accountId);
            coid.setBBobjectID(parts[parts.length - 1]);
            co = companyRepo.findById(coid);
            if (co == null) {
                co = new Company();
                co.objectID = coid;

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

            }
            if (o.company == null || !o.company.objectID.getBBobjectID().equals(co.objectID.getBBobjectID())) {
                o.company = co;
                companyRepo.persist(co);
            }
        }

        SalesUser su;
        BBObjectID suid = new BBObjectID();
        suid.setTenantID(data.accountId);
        String assignToId = KMesg.findField(data, flippedFieldsModel, OpportunityLogicRoles.OPPORTUNITY__ASSIGNED_TO);
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
        if (suid.getBBobjectID() == null || o.assignTo == null || !o.assignTo.objectID.getBBobjectID().equals(suid.getBBobjectID())) {
            su = salesUserRepo.findById(suid);
            if (su == null) {
                su = new SalesUser();
                su.objectID = suid;
                salesUserRepo.persist(su);
            }
            o.assignTo = su;
        }

        if (o.attributes == null) o.attributes = new HashMap<>();
        Map<String, ExtendedAttribute> attributes = o.attributes;
        data.afterBobject.contents.forEach((k, v) -> addAttribute(attributes, data, k, v));

        persist(o);
        return o;

    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, KMesg data, String k, String v) {

        if (v == null) return; // bug panache

        String logicRole = data.frozenModel.opportunity.fieldsModel.get(k);
        OpportunityLogicRoles lrole = OpportunityLogicRoles.NONE;
        if (logicRole != null && !logicRole.equals(""))
            lrole = OpportunityLogicRoles.valueOf(logicRole);

        switch (lrole) {
            case OPPORTUNITY__AMOUNT:
            case OPPORTUNITY__ASSIGNED_TO:
            case OPPORTUNITY__CLOSE_DATE:
            case OPPORTUNITY__COMPANY:
            case OPPORTUNITY__CREATION_DATETIME:
            case OPPORTUNITY__NAME:
            case OPPORTUNITY__STATUS:
            case OPPORTUNITY__TYPE:
                break;
            default:
                ExtendedAttribute attribute = new ExtendedAttribute();
                attribute.assign(lrole, v);
                attributes.put(k, attribute);
                break;
        }
    }

    private int setStatusFromLogicRole(String statusPicklist) {
        int result = Opportunity.OPPORTUNITY__STATUS__NONE;
        switch (statusPicklist) {
            case "OPPORTUNITY__STATUS__CLOSED_WON" -> result = Opportunity.OPPORTUNITY__STATUS__CLOSED_WON;
            case "OPPORTUNITY__STATUS__CLOSED_LOST" -> result = Opportunity.OPPORTUNITY__STATUS__CLOSED_LOST;
            case "OPPORTUNITY__STATUS__FIRST_MEETING_SCHEDULED" -> result = Opportunity.OPPORTUNITY__STATUS__FIRST_MEETING_SCHEDULED;
            case "OPPORTUNITY__STATUS__VERBAL_OK" -> result = Opportunity.OPPORTUNITY__STATUS__VERBAL_OK;
            case "OPPORTUNITY__STATUS__FIRST_MEETING_DONE" -> result = Opportunity.OPPORTUNITY__STATUS__FIRST_MEETING_DONE;
            case "OPPORTUNITY__STATUS__PROPOSAL_EXPLAINED" -> result = Opportunity.OPPORTUNITY__STATUS__PROPOSAL_EXPLAINED;
            case "OPPORTUNITY__STATUS__THIRD_MEETING_DONE" -> result = Opportunity.OPPORTUNITY__STATUS__THIRD_MEETING_DONE;
            case "OPPORTUNITY__STATUS__SECOND_MEETING_DONE" -> result = Opportunity.OPPORTUNITY__STATUS__SECOND_MEETING_DONE;
            case "OPPORTUNITY__STATUS__PROPOSAL_SENT" -> result = Opportunity.OPPORTUNITY__STATUS__PROPOSAL_SENT;
            case "OPPORTUNITY__STATUS__ON_HOLD_NURTURING" -> result = Opportunity.OPPORTUNITY__STATUS__ON_HOLD_NURTURING;
        }

        return result;
    }

    private int setTypeFromLogicRole(String typePicklistID) {
        int result = Opportunity.OPPORTUNITY__TYPE__NONE;
        switch (typePicklistID) {
            case "OPPORTUNITY__TYPE__RENEWAL" -> result = Opportunity.OPPORTUNITY__TYPE__RENEWAL;
            case "OPPORTUNITY__TYPE__UPSELL" -> result = Opportunity.OPPORTUNITY__TYPE__UPSELL;
            case "OPPORTUNITY__TYPE__EXISTING_BUSINESS" -> result = Opportunity.OPPORTUNITY__TYPE__EXISTING_BUSINESS;
            case "OPPORTUNITY__TYPE__NEW_BUSINESS" -> result = Opportunity.OPPORTUNITY__TYPE__NEW_BUSINESS;
        }
        return result;
    }

    public void deleteByCompany(Company c) {
        delete("company",c);
    }
}

