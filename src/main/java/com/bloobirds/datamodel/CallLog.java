package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.Activity;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@DiscriminatorValue("10")
public class CallLog extends Activity {

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    // bug de hibernate y postgres https://shred.zone/cilla/page/299/string-lobs-on-postgresql-with-hibernate-36.html
    public String transcript = "";
    public String origin = "";
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "CtenantID", referencedColumnName = "tenantID"),
            @JoinColumn(name = "CBBobjectID", referencedColumnName = "BBobjectID")
    })
    public Contact contact;
    public Integer seconds = 0;
    public Integer callResult = 0; // ACTIVITY__CALL_STATUS
    // ACTIVITY__CALL_STATUS__ONGOING
    // ACTIVITY__CALL_STATUS__CONNECTING
    // ACTIVITY__CALL_STATUS__ENDED
    public Boolean pitch_done = null;
    public Integer pitch_used = null;
    public Integer direction = null;

    @ElementCollection(fetch = FetchType.EAGER) // Problemas con Jackson, Quarkus, Envers...
    @CollectionTable(
            joinColumns = {@JoinColumn(name = "BBObjectID"), @JoinColumn(name = "tenantID")}
    )

    public Map<String, String> attributes = new HashMap<>();

    public int getActivityType() {
        return Activity.TYPE_CALL;
    }

}
