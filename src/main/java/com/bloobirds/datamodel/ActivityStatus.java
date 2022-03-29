package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.Activity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("0")
public class ActivityStatus extends Activity {


    // ACTIVITY__TYPE_STATUS
    // ACTIVITY__TYPE_STATUS__LEAD_STATUS_CHANGED
    // ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED
    // ACTIVITY__TYPE_STATUS__NEW_LEAD_CREATED
    // ACTIVITY__TYPE_STATUS__COMPANY_STATUS_CHANGED
    // ACTIVITY__TYPE_STATUS__COMPANY_ASSIGNED
    // ACTIVITY__TYPE_STATUS__COMPANY_CREATED
    // ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED_TO_OPPORTUNITY
    // ACTIVITY__TYPE_STATUS__OPPORTUNITY_CREATED
    // ACTIVITY__TYPE_STATUS__OPPORTUNITY_ASSIGNED
    // ACTIVITY__TYPE_STATUS__OPPORTUNITY_STATUS_CHANGED

    public static final int ACTIVITY__TYPE_STATUS__NONE = 0;
    public static final int ACTIVITY__TYPE_STATUS__LEAD_STATUS_CHANGED = 1;
    public static final int ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED = 2;
    public static final int ACTIVITY__TYPE_STATUS__NEW_LEAD_CREATED = 3;
    public static final int ACTIVITY__TYPE_STATUS__COMPANY_STATUS_CHANGED = 4;
    public static final int ACTIVITY__TYPE_STATUS__COMPANY_ASSIGNED = 5;
    public static final int ACTIVITY__TYPE_STATUS__COMPANY_CREATED = 6;
    public static final int ACTIVITY__TYPE_STATUS__NEW_LEAD_ADDED_TO_OPPORTUNITY = 7;
    public static final int ACTIVITY__TYPE_STATUS__OPPORTUNITY_CREATED = 8;
    public static final int ACTIVITY__TYPE_STATUS__OPPORTUNITY_ASSIGNED = 9;
    public static final int ACTIVITY__TYPE_STATUS__OPPORTUNITY_STATUS_CHANGED = 10;

    public String title;
    public int status;
    public String changed_from;
    public String changed_to;
    public boolean automated;


//    ACTIVITY__TYPE_STATUS_CHANGED_FROM
//    	ACTIVITY__TYPE_STATUS_CHANGED_TO
//    	ACTIVITY__STATUS_TITLE


        public int getActivityType() {
            return Activity.ACTIVITY__TYPE__STATUS;
        }
}
