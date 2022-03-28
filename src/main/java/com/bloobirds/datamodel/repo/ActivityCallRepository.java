package com.bloobirds.datamodel.repo;

import com.bloobirds.datamodel.ActivityCall;
import com.bloobirds.datamodel.abstraction.ActivityCallLogicRoles;
import com.bloobirds.datamodel.abstraction.ActivityLogicRoles;
import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.bloobirds.pipelines.messages.KMesg;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import lombok.extern.java.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

import static com.bloobirds.datamodel.abstraction.ActivityLogicRoles.valueOf;

@Log
@ApplicationScoped
public class ActivityCallRepository implements PanacheRepositoryBase<ActivityCall, BBObjectID> {

    private static String findField(KMesg data, Map<String, String> flippedFieldsModel, ActivityCallLogicRoles lrole) {
        String result = "";
        String fID = flippedFieldsModel.get(lrole.name());
        if (fID != null) result = data.afterBobject.contents.get(fID);
        return result;
    }

    @Transactional
    public void newAndPersist(ActivityCall a, KMesg data, Map<String, String> flippedFieldsModel) {
        a.note = findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__NOTE);

        a.origin = findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__CALL_USER_PHONE_NUMBER);

        String duration = findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__CALL_DURATION);
        if (duration != null) {
            try {
                a.seconds = Double.parseDouble(duration);
            } catch (NumberFormatException e) {
            }
        }

        a.callResultFieldID = findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__CALL_RESULT);
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
        a.pitch_doneFieldID = findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__PITCH_DONE);
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
            a.pitch_used = findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__PITCH);
        }

        a.directionFieldID = findField(data, flippedFieldsModel, ActivityCallLogicRoles.ACTIVITY__DIRECTION);
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

        a.attributes = new HashMap<>();
        data.afterBobject.contents.forEach((k, v) -> addAttribute(a.attributes, data.frozenModel.activity.fieldsModel.get(k), k, v));
        persist(a);
    }

    private void addAttribute(Map<String, ExtendedAttribute> attributes, String logicRole, String k, String v) {

        if (v == null) return; // BUG Pnache! no podemos guardar los null o el persist no hace update y da duplicate key

        if (logicRole != null && !logicRole.equals("")) {

            ActivityLogicRoles lrole = null;
            ActivityCallLogicRoles clrole = null;

            try {
                lrole = valueOf(logicRole);
            } catch (IllegalArgumentException w) {
                clrole = ActivityCallLogicRoles.valueOf(logicRole);
            }

            if (lrole != null) {
                switch (lrole) {
                    case ACTIVITY__CHANNEL:
                    case ACTIVITY__COMPANY:
                    case ACTIVITY__CREATION_DATETIME:
                    case ACTIVITY__LEAD:
                    case ACTIVITY__TIME:
                    case ACTIVITY__TYPE:
                    case ACTIVITY__UPDATE_DATETIME:
                    case ACTIVITY__USER:
                        break;
                    default: {
                        ExtendedAttribute attribute = attributes.get(k);
                        if (attribute == null) attribute = new ExtendedAttribute();
                        attribute.assign(lrole, v);
                        attributes.put(k, attribute);
                        break;
                    }
                }
            } else if (clrole != null) {
                switch (clrole) {
                    case ACTIVITY__CALL_USER_PHONE_NUMBER:
                    case ACTIVITY__CALL_DURATION:
                    case ACTIVITY__CALL_RESULT:
                    case ACTIVITY__PITCH_DONE:
                    case ACTIVITY__PITCH:
                    case ACTIVITY__DIRECTION:
                    case ACTIVITY__NOTE:
                        break;
                    default: {
                        ExtendedAttribute attribute = attributes.get(k);
                        if (attribute == null) attribute = new ExtendedAttribute();
                        attribute.assign(clrole, v);
                        attributes.put(k, attribute);
                        break;
                    }
                }
            } else {
                ExtendedAttribute attribute = attributes.get(k);
                if (attribute == null) attribute = new ExtendedAttribute();
                attributes.put(k, attribute);
            }
        }
    }
}
