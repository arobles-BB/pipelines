package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.BBObjectID;


import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class SalesUser {

    @EmbeddedId
    public BBObjectID objectID = new BBObjectID();

}
