package com.luhonghai.hpx.jdo;

import javax.jdo.annotations.*;
import java.util.Collection;
import java.util.Date;

/**
 * Created by luhonghai on 4/22/14.
 */

@PersistenceCapable
public class Patient {

    @PrimaryKey
    @Persistent(customValueStrategy="uuid")
    private String id;

    @Persistent
    private String firstName;

    @Persistent
    private String lastName;

    @Persistent
    private Date birthDay;

    @Persistent
    private boolean gender;

    @Persistent
    private String description;

    @Persistent
    private String address;

    @Persistent
    private Date createdDate;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }
}
