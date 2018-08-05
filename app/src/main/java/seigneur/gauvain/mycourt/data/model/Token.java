package seigneur.gauvain.mycourt.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity
public class Token {

    @PrimaryKey()
    public int id;

    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("scope")
    public String tokenScope;

    /*@SerializedName("created_at")
    public int tokendateCreation;*/

    public Token(){}

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getTokenScope() {
        return tokenScope;
    }

   /* public int getTokendateCreation() {
        return tokendateCreation;
    }*/



}
