package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.Activity;

import javax.persistence.*;


@Entity
@DiscriminatorValue("13")
public class Meeting extends Activity {


    public int meetingResults;
    public int source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "SUtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "SUobjectID", referencedColumnName = "BBobjectID")
    })
    public SalesUser assignTo;

    public String getAssignToFullName() {
        if (assignTo == null) return "";
        else return assignTo.getFullName();
    }

    public int getActivityType() {
        return Activity.TYPE_MEETING;
    }

}
