package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Company {

    public final static int BBOBJECT_TYPE=20;

    @EmbeddedId
    public BBObjectID objectID = new BBObjectID();

    public String name; //COMPANY__NAME
    @Audited
    public int status; //COMPANY__STATUS

    @Transient
    public LocalDate startedToProspect; //COMPANY__STATUS__CHANGED_DATE_READY_TO_PROSPECT

    public int discardedReasons; // COMPANY__DISCARDED_REASONS
    public int nurturingReasons; // COMPANY__NURTURING_REASONS
    public int source; // COMPANY__SOURCE
    public int targetMarket; // COMPANY__TARGET_MARKET
    public int country; // COMPANY__COUNTRY
    public int industry; // COMPANY__INDUSTRY
    public int vertical; // ??
    public int employeeRange; // COMPANY__SIZE
    public int scenario; // COMPANY__SCENARIO


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "SUtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "SUobjectID", referencedColumnName = "BBobjectID")
    })
    public SalesUser assignTo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            joinColumns = {@JoinColumn(name = "BBobjectID"), @JoinColumn(name = "tenantID")}
    )
    @ToString.Exclude
    public Map<String, String> attributes = new HashMap<>();

    @JsonIgnore
    public String getAssignToFullName(){
        if (assignTo == null) return "";
        else return assignTo.getFullName();
    }
}

