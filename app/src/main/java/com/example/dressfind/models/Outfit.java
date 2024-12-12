package com.example.dressfind.models;

import java.util.Date;

public class Outfit {

    private String OutfitId;
    private String UserId;
    private String Name;
    private Date CreationDate;
    private String Description;
    private Date ScheduledDate;

    public Outfit() {
    }

    public Outfit(String outfitId, String userId, String name, Date creationDate, String description, Date scheduledDate) {
        OutfitId = outfitId;
        UserId = userId;
        Name = name;
        CreationDate = creationDate;
        Description = description;
        ScheduledDate = scheduledDate;
    }

    public String getOutfitId() {
        return OutfitId;
    }

    public void setOutfitId(String outfitId) {
        OutfitId = outfitId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Date getCreationDate() {
        return CreationDate;
    }

    public void setCreationDate(Date creationDate) {
        CreationDate = creationDate;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Date getScheduledDate() {
        return ScheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        ScheduledDate = scheduledDate;
    }
}
