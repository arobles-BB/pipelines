package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.bloobirds.datamodel.abstraction.ExtendedAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Contact {
    public static final int STATUS_NO_STATUS = 0;
    public static final int STATUS_NEW = 1;    // LEAD__STATUS__NEW
    public static final int STATUS_DELIVERED = 2;  // LEAD__STATUS__DELIVERED
    public static final int STATUS_ON_PROSPECTION = 3; // LEAD__STATUS__ON_PROSPECTION
    public static final int STATUS_CONTACTED = 4; // LEAD__STATUS__CONTACTED
    public static final int STATUS_ENGAGED = 5; // LEAD__STATUS__ENGAGED
    public static final int STATUS_MEETING = 6; // LEAD__STATUS__MEETING
    public static final int STATUS_NURTURING = 7; // LEAD__STATUS__NURTURING
    public static final int STATUS_DISCARDED = 8; // LEAD__STATUS__DISCARDED
    public static final int STATUS_CONTACT = 9;    // LEAD__STATUS__CONTACT
    public static final int STATUS_BACKLOG = 10;    // LEAD__STATUS__BACKLOG
    final static int BBOBJECT_TYPE = 40;
    public String name; // LEAD__NAME
    public String surname; // LEAD__SURNAME
    public String jobTitle; // LEAD__LINKEDIN_JOB_TITLE
    public String linkedIn; // LEAD__LINKEDIN_URL
    public String phoneNumber; // LEAD__PHONE
    public String email; // LEAD__EMAIL
    @Audited
    public int status; // LEAD__STATUS

    @EmbeddedId
    public BBObjectID objectID = new BBObjectID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "COtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "COobjectID", referencedColumnName = "BBobjectID")
    })
    public Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "SUtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "SUobjectID", referencedColumnName = "BBobjectID")
    })
    public SalesUser assignTo;

    @ElementCollection(fetch = FetchType.EAGER) // Problemas con Quarkus y Envers
    @CollectionTable(
            joinColumns = {@JoinColumn(name = "BBobjectID"), @JoinColumn(name = "tenantID")}
    )
    @ToString.Exclude
    public Map<String, ExtendedAttribute> attributes = new HashMap<>();

    @JsonIgnore
    public String getAssignToFullName() {
        if (assignTo == null) return "";
        else return assignTo.getFullName();
    }

    @JsonIgnore
    public String getFullName() {
        return MessageFormat.format("{0} {1}", name, surname);
    }

    public void setStatusWithLRole(String statusPicklist) {

        switch (statusPicklist) {
            case "LEAD__STATUS__NEW":
                this.status = STATUS_NEW;
                break;
            case "LEAD__STATUS__DELIVERED":
                this.status = STATUS_DELIVERED;
                break;
            case "LEAD__STATUS__ON_PROSPECTION":
                this.status = STATUS_ON_PROSPECTION;
                break;
            case "LEAD__STATUS__CONTACTED":
                this.status = STATUS_CONTACTED;
                break;
            case "LEAD__STATUS__ENGAGED":
                this.status = STATUS_ENGAGED;
                break;
            case "LEAD__STATUS__MEETING":
                this.status = STATUS_MEETING;
                break;
            case "LEAD__STATUS__NURTURING":
                this.status = STATUS_NURTURING;
                break;
            case "LEAD__STATUS__DISCARDED":
                this.status = STATUS_DISCARDED;
                break;
            case "LEAD__STATUS__CONTACT":
                this.status = STATUS_CONTACT;
                break;
            case "LEAD__STATUS__BACKLOG":
                this.status = STATUS_BACKLOG;
                break;
            default:
                this.status = STATUS_NO_STATUS;
        }
    }
}
