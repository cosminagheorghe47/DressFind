package com.example.dressfind.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class Outfit implements Parcelable {

    private String OutfitId;
    private String UserId;
    private String Name;
    private Date CreationDate;
    private String Description;
    private Date ScheduledDate;
    private String Image;
    private boolean IsPublic;

    public Outfit() {
    }

    public Outfit(String outfitId, String userId, String name, Date creationDate, String description, Date scheduledDate, String image, boolean isPublic) {
        OutfitId = outfitId;
        UserId = userId;
        Name = name;
        CreationDate = creationDate;
        Description = description;
        ScheduledDate = scheduledDate;
        Image = image;
        IsPublic = isPublic;
    }

    protected Outfit(Parcel in) {
        OutfitId = in.readString();
        UserId = in.readString();
        Name = in.readString();
        Description = in.readString();
        Image = in.readString();
    }

    public static final Creator<Outfit> CREATOR = new Creator<Outfit>() {
        @Override
        public Outfit createFromParcel(Parcel in) {
            return new Outfit(in);
        }

        @Override
        public Outfit[] newArray(int size) {
            return new Outfit[size];
        }
    };

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
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

    public boolean isPublic() {
        return IsPublic;
    }

    public void setPublic(boolean aPublic) {
        IsPublic = aPublic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(OutfitId);
        dest.writeString(UserId);
        dest.writeString(Name);
        dest.writeString(Description);
        dest.writeString(Image);
    }

    @Override
    public String toString() {
        return "Outfit{" +
                "OutfitId='" + OutfitId + '\'' +
                ", UserId='" + UserId + '\'' +
                ", Name='" + Name + '\'' +
                ", CreationDate=" + CreationDate +
                ", Description='" + Description + '\'' +
                ", ScheduledDate=" + ScheduledDate +
                ", Image='" + Image + '\'' +
                ", isPublic=" + IsPublic +
                '}';
    }
}
