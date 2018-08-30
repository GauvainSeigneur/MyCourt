package seigneur.gauvain.mycourt.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Map;

@Entity
public class User {

    @PrimaryKey
    @SerializedName("id")
    public int id;
    public String name;
    public String login;
    public String html_url;
    public String avatar_url;
    public String bio;
    public String location;
    public Map<String, String> links;
    @SerializedName("can_upload_shot")
    public boolean isAllowedToUpload;
    @SerializedName("pro")
    public boolean isPro;
    @SerializedName("followers_count")
    public int followers_count;
    public String type;
    public String created_at;
    public String cryptedPwd;

    //todo - for phase 3: manage teams

    public User(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public boolean isAllowedToUpload() {
        return isAllowedToUpload;
    }

    public void setAllowedToUpload(boolean allowedToUpload) {
        isAllowedToUpload = allowedToUpload;
    }

    public boolean isPro() {
        return isPro;
    }

    public void setPro(boolean pro) {
        isPro = pro;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(int followers_count) {
        this.followers_count = followers_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCryptedPwd() {
        return cryptedPwd;
    }

    public void setCryptedPwd(String cryptedPwd) {
        this.cryptedPwd = cryptedPwd;
    }
}
