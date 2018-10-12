package seigneur.gauvain.mycourt.data.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Relation;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

@Entity
public class Draft {

    @PrimaryKey(autoGenerate = true)
    public long draftID; //Insert methods treat 0 as not-set while inserting the item, so Room will auto generated an ID
    public int typeOfDraft; //NEW SHOT OR UPDATE
    public String imageUri; //for viewing TODO - maybe delete ?
    public String imageFormat;
    public Date schedulingDate; //ONLY FOR UPDATE OF AN ALREADY PUBLISHED SHOT
    //Embed shot object
    @Embedded
    public Shot shot;

    //empty constructor
    @Ignore
    public Draft(){}

    //Constructor for Database
    public Draft(
            int typeOfDraft,
            @Nullable String imageUri,
            @Nullable String imageFormat,
            @Nullable  Date schedulingDate,
            Shot shot) {
        this.typeOfDraft=typeOfDraft;
        this.imageUri=imageUri;
        this.imageFormat=imageFormat;
        this.schedulingDate=schedulingDate;
        this.shot=shot;
    }

    public long getDraftID() {
        return draftID;
    }

    public void setDraftID(int draftID) {
        this.draftID = draftID;
    }

    public int getTypeOfDraft() {
        return typeOfDraft;
    }

    public void setTypeOfDraft(int typeOfDraft) {
        this.typeOfDraft = typeOfDraft;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public Date getSchedulingDate() {
        return schedulingDate;
    }

    public void setSchedulingDate(Date schedulingDate) {
        this.schedulingDate = schedulingDate;
    }

    public Shot getShot() {
        return shot;
    }

    public void setShot(Shot shot) {
        this.shot = shot;
    }

    public void changeInfoFromEdit(
            @Nullable String inImageUri,
            @Nullable String inImageFormat,
            String title,
            String desc,
            ArrayList<String> tags) {
        imageUri = inImageUri;
        imageFormat = inImageFormat;
        shot.setTitle(title);
        shot.setDescription(desc);
        shot.setTagList(tags);
    }


}