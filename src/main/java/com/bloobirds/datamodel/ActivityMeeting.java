package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.Activity;

import javax.persistence.*;


@Entity
@DiscriminatorValue("3")
public class ActivityMeeting extends Activity {
//    ACTIVITY__MEETING_ID
//    ACTIVITY__MEETING_RESULT
//    ACTIVITY__MEETING_TITLE
//    MEETING__TYPE
//    ACTIVITY__ACCOUNT_EXECUTIVE,

    public int meetingResults;
    public int source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "MEtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "MEobjectID", referencedColumnName = "BBobjectID")
    })
    public SalesUser assignTo;

    public String getAssignToFullName() {
        if (assignTo == null) return "";
        else return assignTo.getFullName();
    }

    public int getActivityType() {
        return Activity.ACTIVITY__TYPE__MEETING;
    }

}
