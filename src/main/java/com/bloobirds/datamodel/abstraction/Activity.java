package com.bloobirds.datamodel.abstraction;

import com.bloobirds.datamodel.SalesUser;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Cacheable // all its field values are cached except for collections and relations to other entities
@Table(indexes = @Index(columnList = "date, icp, targetmarket"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // we can’t use "not null" constraints on subclass attributes (check the risk of data inconsistencies)
@DiscriminatorColumn(name ="activity_type")
public abstract class Activity extends PanacheEntityBase {

    public static int TYPE_CALL=10;
    public static int TYPE_EMAIL=11;
    public static int TYPE_LINKEDIN=12;
    public static int TYPE_MEETING=13;


    // mucho cuidado con la estrategia de creación de IDs en herencia por que el Discriminator no es parte de la key

    @EmbeddedId
    public BBObjectID objectID = new BBObjectID();

    @Temporal(TemporalType.DATE)
    public java.util.Date date; // ??
    public int targetMarket; //
    public int icp; //
    public int scenario; //
    public int channel; //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "UStenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "USobjectID", referencedColumnName = "BBobjectID")
    })
    public SalesUser user;



    public String getUserFullName() {
        if (user == null) return "";
        else return user.getFullName();
    }

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            joinColumns = {@JoinColumn(name = "BBObjectID"), @JoinColumn(name = "tenantID")}
    )
    @ToString.Exclude
    public Map<String, String> attributes = new HashMap<>();

    public abstract int getActivityType();
}

