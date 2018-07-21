package seigneur.gauvain.mycourt.data.model;

import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Shot {

    //Dribbble API
    public static final String IMAGE_NORMAL = "normal";
    public static final String IMAGE_HIDPI = "hidpi";
    public static final String IMAGE_TEASER = "teaser";
    @PrimaryKey
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
    public User user;

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
    public String getTeaserUrl() {
        if (images == null) {
            return "";
        }
        String url = images.containsKey(IMAGE_TEASER) && images.get(IMAGE_TEASER)!=null
                ? images.get(IMAGE_TEASER)
                : images.get(IMAGE_TEASER);
        return url == null ? "" : url;

    }

    public boolean isLow_profile() {
        return low_profile;
    }

    /*public void setLow_profile(boolean low_profile) {
        this.low_profile = low_profile;
    }*/

    public String getId() {
        return id;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public ArrayList<String> getTagList() {
        return tagList;
    }
}
