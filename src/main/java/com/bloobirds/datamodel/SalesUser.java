package com.bloobirds.datamodel;

import com.bloobirds.datamodel.abstraction.BBObjectID;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
public class SalesUser {

    @EmbeddedId
    public BBObjectID objectID = new BBObjectID();

}
