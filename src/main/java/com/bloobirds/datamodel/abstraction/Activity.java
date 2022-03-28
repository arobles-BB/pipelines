package com.bloobirds.datamodel.abstraction;

import com.bloobirds.datamodel.Company;
import com.bloobirds.datamodel.Contact;
import com.bloobirds.datamodel.SalesUser;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.CascadeType;


import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Cacheable // all its field values are cached except for collections and relations to other entities
@Table(indexes = @Index(columnList = "date, icp, targetmarket"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// we can’t use "not null" constraints on subclass attributes (check the risk of data inconsistencies)
@DiscriminatorColumn(name = "activity_type")
public abstract class Activity extends PanacheEntityBase {

    public static final int ACTIVITY__TYPE__STATUS = 0;
    public static final int ACTIVITY__TYPE__INBOUND = 1;
    public static final int ACTIVITY__TYPE__NOTE = 2;
    public static final int ACTIVITY__TYPE__MEETING = 3;
    public static final int ACTIVITY__TYPE__LINKEDIN_MESSAGE = 4;
    public static final int ACTIVITY__TYPE__EMAIL = 5;
    public static final int ACTIVITY__TYPE__CADENCE = 6;
    public static final int ACTIVITY__TYPE__CALL = 7;

    public static final int ACTIVITY__CHANNEL__OTHER = 0;
    public static final int ACTIVITY__CHANNEL__EMAIL = 1;
    public static final int ACTIVITY__CHANNEL__LINKEDIN_MESSAGE = 2;
    public static final int ACTIVITY__CHANNEL__CALL = 3;

    // mucho cuidado con la estrategia de creación de IDs en herencia por que el Discriminator no es parte de la key

//    ACTIVITY__DATA_SOURCE_AUTOMATED,
//    ACTIVITY__USER_EMPLOYEE_ROLE,
//    ACTIVITY__NOTIFICATION_OWNER_EMPLOYEE_ROLE,
//    ACTIVITY__TABLE_VIEW_ITEM,
//    ACTIVITY__REPORTED,
//    ACTIVITY__ICON_COMPANION,
//    ACTIVITY__OPPORTUNITY,
//    ACTIVITY__NOTIFICATION_OWNER,
//    ACTIVITY__ICON,
//    ACTIVITY__IS_NOTIFICATION,
//    ACTIVITY__DATA_SOURCE,
//    ACTIVITY__IMPORT_ID,
//    ACTIVITY__IS_ATTEMPT,
//    ACTIVITY__IS_PINNED,
//    ACTIVITY__IS_TOUCH,
//    HUBSPOT__ENGAGEMENT_ID

    @EmbeddedId
    public BBObjectID objectID = new BBObjectID();

    @Temporal(TemporalType.DATE)
    public java.util.Date date; // ACTIVITY__TIME vs ACTIVITY__CREATION_DATETIME vs. ACTIVITY__UPDATE_DATETIME
    public String targetMarket; // from Company
    public String icp; // from Company
    public String scenario; //  from Company
    public int channel; // ACTIVITY__CHANNEL
    public String channelID;
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "SUtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "SUobjectID", referencedColumnName = "BBobjectID")
    })
    public SalesUser user; // ACTIVITY__USER

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "COtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "COobjectID", referencedColumnName = "BBobjectID")
    })
    public Company company;     // ACTIVITY__COMPANY

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "LEtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "LEobjectID", referencedColumnName = "BBobjectID")
    })
    public Contact lead;     // ACTIVITY__LEAD
    // ACTIVITY__LEAD_EMAIL

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            joinColumns = {@JoinColumn(name = "BBObjectID"), @JoinColumn(name = "tenantID")}
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "BBObjectID")
    @Cascade(value= {CascadeType.ALL})
    @ToString.Exclude
    public Map<String, ExtendedAttribute> attributes = new HashMap<>();

    public abstract int getActivityType(); //    ACTIVITY__TYPE,
}

