package com.bloobirds.datamodel.abstraction;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.java.Log;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Log
@Embeddable
public class ExtendedAttribute {
    public String logicRole;
    public String stringValue;
    @Temporal(TemporalType.TIMESTAMP)
    public Date dateValue;

    public Long numericValue;

    public void assign(ContactLogicRoles lrole, String v) {
        if (v == null) return;
        logicRole = lrole.name();
        stringValue = v;

        try {
            LocalDateTime dateToConvert = LocalDateTime.parse(v, DateTimeFormatter.ISO_DATE_TIME);
            dateValue = java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault())
                            .toInstant());
        } catch (DateTimeParseException e) {
        }

        try {
            numericValue = Long.parseLong(v);
        } catch (NumberFormatException e) {
        }

    }
}
