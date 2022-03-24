package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Company {

    public final static int BBOBJECT_TYPE=20;

    public static final int COMPANY__STATUS__OTHER= 0;
    public static final int COMPANY__STATUS__NEW= 1;
    public static final int COMPANY__STATUS__DELIVERED= 2;
    public static final int COMPANY__STATUS__ON_PROSPECTION= 3;
    public static final int COMPANY__STATUS__READY_TO_PROSPECT= 4;
    public static final int COMPANY__STATUS__FINDING_LEADS= 2;
    public static final int COMPANY__STATUS__CONTACTED= 3;
    public static final int COMPANY__STATUS__ENGAGED= 4;
    public static final int   COMPANY__STATUS__MEETING= 5;
    public static final int COMPANY__STATUS__CLIENT= 6;
    public static final int COMPANY__STATUS__NURTURING= 7;
    public static final int COMPANY__STATUS__DISCARDED= 8;
    public static final int COMPANY__STATUS__ACCOUNT= 9;
    public static final int COMPANY__STATUS__BACKLOG= 10;

    public static final int COMPANY__SOURCE__OTHER=0;
    public static final int COMPANY__SOURCE__OUTBOUND=1;
    public static final int COMPANY__SOURCE__INBOUND=2;

    @EmbeddedId
    public BBObjectID objectID = new BBObjectID();

    public String name; //COMPANY__NAME
    @Audited
    public int status; //COMPANY__STATUS
    @Audited
    public String statusPicklistID; // ID en caso de que no sea uno con logic role

    @Transient
    public LocalDateTime startedToProspect; //COMPANY__STATUS__CHANGED_DATE_READY_TO_PROSPECT

    public String discardedReasons; // COMPANY__DISCARDED_REASONS
    public String nurturingReasons; // COMPANY__NURTURING_REASONS
    public int source; // COMPANY__SOURCE
    public String sourcePicklistID;
    public String targetMarket; // COMPANY__TARGET_MARKET
    public String country; // COMPANY__COUNTRY
    public String industry; // COMPANY__INDUSTRY
    public int vertical; // ??
    public String employeeRange; // COMPANY__SIZE
    public String scenario; // COMPANY__SCENARIO


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "SUtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "SUobjectID", referencedColumnName = "BBobjectID")
    })
    public SalesUser assignTo; // COMPANY__ASSIGNED_TO

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            joinColumns = {@JoinColumn(name = "BBobjectID"), @JoinColumn(name = "tenantID")}
    )
    @ToString.Exclude
    public Map<String, ExtendedAttribute> attributes = new HashMap<>();



    @JsonIgnore
    public String getAssignToFullName(){
        if (assignTo == null) return "";
        else return assignTo.getFullName();
    }
}

