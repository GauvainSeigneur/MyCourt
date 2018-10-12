package seigneur.gauvain.mycourt.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Shot {
    //todo - finish this class, add team ?

    //Dribbble API
    public static final String IMAGE_NORMAL = "normal";
    public static final String IMAGE_HIDPI = "hidpi";
    public static final String IMAGE_TEASER = "teaser";
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;
    public String title;
    public String description;
    public String html_url;
    public int width;
    public int height;
    public Map<String, String> images;
    public boolean animated;
    @SerializedName("tags")
    public ArrayList<String> tagList;
    public boolean low_profile;
    @SerializedName("published_at")
    public Date publishDate;
    @SerializedName("updated_at")
    public Date updateDate;

    //Empty Constructor
    public Shot(){}

    //Constructor for drafts update of the Shot
    public Shot(@Nullable String id, @Nullable String title,
                @Nullable String desc,@Nullable ArrayList<String> tags) {
        this.id=id;
        this.title=title;
        this.description=desc;
        this.tagList=tags;
    }

    //getter
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    //todo - to be replaced by getImageHidpi
    @NonNull
    public String getImageUrl() {
        if (images == null) {
            return "";
        }
        String url = images.containsKey(IMAGE_HIDPI) && images.get(IMAGE_HIDPI)!=null
                ? images.get(IMAGE_HIDPI)
                : images.get(IMAGE_NORMAL);
        return url == null ? "" : url;
    }

    @NonNull
    public String getImageHidpi() {
        if (images == null) {
            return "";
        }
        String url = images.containsKey(IMAGE_HIDPI) && images.get(IMAGE_HIDPI) != null
                ? images.get(IMAGE_HIDPI)
                : images.get(IMAGE_NORMAL);
        return url == null ? "" : url;
    }

    @NonNull
    public String getImageNormal() {
        if (images == null) {
            return "";
        }

        String imageUrlNormal =  images.get(IMAGE_NORMAL);
        return imageUrlNormal == null ? "" : imageUrlNormal;
    }

    @NonNull
    public String getImageTeaser() {
        if (images == null) {
            return "";
        }

        String url =  images.get(IMAGE_TEASER);
        return url == null ? "" : url;
    }

    public boolean isLow_profile() {
        return low_profile;
    }

    public String getId() {
        return id;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public ArrayList<String> getTagList() {
        return tagList;
    }

    // SETTER
    //ONLY THESE FIELDS CAN BE CHANGED IN ORDER TO UPDATE THE SHOT TO DRIBBBLE
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTagList(ArrayList<String> tagList) {
        this.tagList = tagList;
    }

}
