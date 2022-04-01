package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.ActivityCall;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.datamodel.abstraction.logicroles.ActivityCallLogicRoles;
import com.bloobirds.pipelines.messages.KMesg;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import lombok.extern.java.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Log
@ApplicationScoped
public class ActivityCallRepository implements PanacheRepositoryBase<ActivityCall, BBObjectID> {

    @Transactional
    public void newAndUpdate(ActivityCall a, KMesg data, Map<String, String> flippedFieldsModel) {
        a.note = KMesg.findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__NOTE);

        a.origin = KMesg.findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__CALL_USER_PHONE_NUMBER);

        String duration = KMesg.findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__CALL_DURATION);
        if (duration != null) {
            try {
                a.seconds = Double.parseDouble(duration);
            } catch (NumberFormatException e) {
            }
        }

        a.callResultFieldID = KMesg.findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__CALL_RESULT);
        if (a.callResultFieldID != null) {
            String callResultPicklistID = data.frozenModel.activity.picklistsModel.get(a.callResultFieldID);
            if (callResultPicklistID != null) {
                switch (callResultPicklistID) {
                    case "ACTIVITY__CALL_RESULT__GATEKEEPER" -> a.callResult = ActivityCall.ACTIVITY__CALL_RESULT__GATEKEEPER;
                    case "ACTIVITY__CALL_RESULT__CORRECT_CONTACT" -> a.callResult = ActivityCall.ACTIVITY__CALL_RESULT__CORRECT_CONTACT;
                    case "ACTIVITY__CALL_RESULT__REFERRAL" -> a.callResult = ActivityCall.ACTIVITY__CALL_RESULT__REFERRAL;
                    case "ACTIVITY__CALL_RESULT__LEFT_VOICEMAIL" -> a.callResult = ActivityCall.ACTIVITY__CALL_RESULT__LEFT_VOICEMAIL;
                    case "ACTIVITY__CALL_RESULT__NO_ANSWER" -> a.callResult = ActivityCall.ACTIVITY__CALL_RESULT__NO_ANSWER;
                    case "ACTIVITY__CALL_RESULT__BUSY" -> a.callResult = ActivityCall.ACTIVITY__CALL_RESULT__BUSY;
                    case "ACTIVITY__CALL_RESULT__APPROACH" -> a.callResult = ActivityCall.ACTIVITY__CALL_RESULT__APPROACH;
                    default -> a.callResult = -1;
                }
            }
        }
        a.pitch_doneFieldID = KMesg.findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__PITCH_DONE);
        if (a.pitch_doneFieldID != null) {
            String pitchDondePicklistID = data.frozenModel.activity.picklistsModel.get(a.pitch_doneFieldID);
            if (pitchDondePicklistID != null) {
                switch (pitchDondePicklistID) {
                    case "ACTIVITY__PITCH_DONE__YES" -> a.pitch_done = true;
                    case "ACTIVITY__PITCH_DONE__NO" -> a.pitch_done = false;
                }
            }
        }
        if (a.pitch_done) {
            a.pitch_used = KMesg.findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__PITCH);
        }

        a.directionFieldID = KMesg.findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__DIRECTION);
        if (a.directionFieldID != null) {
            String directionPicklistID = data.frozenModel.activity.picklistsModel.get(a.directionFieldID);

            if (directionPicklistID != null) {

                switch (directionPicklistID) {
                    case "ACTIVITY__DIRECTION__INCOMING" -> a.direction = ActivityCall.ACTIVITY__DIRECTION__INCOMING;
                    case "ACTIVITY__DIRECTION__MISSED" -> a.direction = ActivityCall.ACTIVITY__DIRECTION__MISSED;
                    case "ACTIVITY__DIRECTION__OUTGOING" -> a.direction = ActivityCall.ACTIVITY__DIRECTION__OUTGOING;
                    default -> a.direction = -1;
                }
            }
        }

        if (a.attributes == null) a.attributes = new HashMap<>();
        data.afterBobject.contents.forEach((k, v) -> addAttribute(a.attributes, data, k, v));
        persist(a);
    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, KMesg data, String k, String v) {
        if (ActivityRepository.addAttribute(attributes, data, k, v) != null) return;
        if (v == null) {
            attributes.remove(k);
            return; // BUG Pnache! no podemos guardar los null o el persist no hace update y da duplicate key
        }
        String logicRole = data.frozenModel.activity.fieldsModel.get(k);

        ActivityCallLogicRoles lrole;

        switch (logicRole) {
            case "ACTIVITY__CALL_USER_PHONE_NUMBER":
            case "ACTIVITY__CALL_DURATION":
            case "ACTIVITY__CALL_RESULT":
            case "ACTIVITY__PITCH_DONE":
            case "ACTIVITY__PITCH":
            case "ACTIVITY__DIRECTION":
            case "ACTIVITY__NOTE":
                break;
            default:
                ExtendedAttribute attribute = new ExtendedAttribute();
                if (logicRole != null && !logicRole.equals("")) {
                    try {
                        lrole = ActivityCallLogicRoles.valueOf(logicRole);
                        attribute.assign(lrole, v);
                    } catch (Exception e) {
                        log.info("Invalid Data in Logic Role:" + e.getMessage());
                    }
                }
                attributes.put(k, attribute);
                break;
        }

    }
}
