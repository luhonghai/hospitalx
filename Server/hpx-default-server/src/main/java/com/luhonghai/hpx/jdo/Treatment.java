package com.luhonghai.hpx.jdo;

import javax.jdo.annotations.*;
import java.util.Date;

/**
 * Created by Hai Lu on 22/04/2014.
 */

@PersistenceCapable
public class Treatment {
    @PrimaryKey
    @Persistent(customValueStrategy="uuid")
    private String id;

    @Persistent
    private String type;

    @Persistent
    private Date createdDate;

    @NotPersistent
    private String patient_id;

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }
}
