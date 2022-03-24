package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.BBObjectID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;

import javax.persistence.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Entity
public class SalesUser {

    public final static int BBOBJECT_TYPE=30;

    public String name;
    public String surname;
    public String phoneNumber;
    public String email;

    public static final int STATUS_NO_STATUS = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 2;

    public int status;

    @EmbeddedId
    public BBObjectID objectID = new BBObjectID();

    @JsonIgnore
    public String getFullName(){
        return MessageFormat.format("{0} {1}",name,surname);
    }

    @ElementCollection(fetch = FetchType.EAGER) // Problemas con Jackson, Quarkus, Envers...
    @CollectionTable(
            joinColumns = {@JoinColumn(name = "BBObjectID"), @JoinColumn(name = "tenantID")}
    )
    @ToString.Exclude
    public Map<String, String> attributes = new HashMap<>();

}
