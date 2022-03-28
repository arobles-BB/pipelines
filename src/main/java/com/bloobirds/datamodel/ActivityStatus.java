package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.Activity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("0")
public class ActivityStatus extends Activity {
//    ACTIVITY__TYPE_STATUS_CHANGED_FROM
//    	ACTIVITY__TYPE_STATUS
//    	ACTIVITY__TYPE_STATUS_CHANGED_TO
//    	ACTIVITY__STATUS_TITLE
        public int getActivityType() {
            return Activity.ACTIVITY__TYPE__STATUS;
        }
}
