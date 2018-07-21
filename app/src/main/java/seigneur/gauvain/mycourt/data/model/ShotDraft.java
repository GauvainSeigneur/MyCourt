package seigneur.gauvain.mycourt.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import seigneur.gauvain.mycourt.utils.Constants;

@Entity
public class ShotDraft {
    //Dribbble API
    public String shotId;
    public String imageUri;
    public String title;
    public String description;
    public boolean isLowProfile;
    public Date schedulingDate;
    public ArrayList<String> tags; //Max twelve
    public int teamId;
    public Date dateOfPublication;
    public Date dateOfUpdate;
    //Internal of the application
    //For ROOM
    @PrimaryKey(autoGenerate = true)
    public int id; //Insert methods treat 0 as not-set while inserting the item, so Room will auto generated an ID
    public int typeOfDraft; //NEW SHOT OR UPDATE

    //constructor
    @Ignore
    public ShotDraft(){}

    //Constructor for Database
    public ShotDraft(
                     int id,
                     @Nullable String imageUri,
                     String shotId,
                     String title,
                     String description,
                     boolean isLowProfile,
                     @Nullable  Date schedulingDate,
                     @Nullable ArrayList<String> tags,
                     int teamId,
                     int typeOfDraft,
                     @Nullable Date dateOfPublication,
                     @Nullable Date dateOfUpdate) {
        this.id=id;
        this.shotId=shotId;
        this.imageUri=imageUri;
        this.title=title;
        this.description=description;
        this.isLowProfile=isLowProfile;
        this.schedulingDate=schedulingDate;
        this.tags=tags;
        this.teamId=teamId;
        this.typeOfDraft=typeOfDraft;
        this.dateOfPublication=dateOfPublication;
        this.dateOfUpdate=dateOfUpdate;
    }

    public int getId() {
        return id;
    }

    public int getDraftType() {
        return typeOfDraft;
    }

    public String getImageUrl() {
        return imageUri;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLowProfile() {
        return isLowProfile;
    }

    public Date getSchedulingDate() {
        return schedulingDate;
    }

    public ArrayList<String> getTagList() {
        return tags;
    }

    public int getTeamId() {
        return teamId;
    }

    public Date getDateOfPublication() {
        return dateOfPublication;
    }

    public Date getDateOfUpdate() {
        return dateOfUpdate;
    }

    public String getShotId() {
        return shotId;
    }

}