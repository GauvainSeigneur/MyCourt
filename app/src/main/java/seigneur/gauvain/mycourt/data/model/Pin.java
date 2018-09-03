package seigneur.gauvain.mycourt.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

//insert relation : https://stackoverflow.com/questions/44667160/android-room-insert-relation-entities-using-room
@Entity
public class Pin {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String cryptedPIN;

    public String initVector;

    public Pin(int id, String cryptedPIN, String initVector) {
        this.id =id;
        this.cryptedPIN=cryptedPIN;
        this.initVector=initVector;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCryptedPIN() {
        return cryptedPIN;
    }

    public void setCryptedPIN(String cryptedPIN) {
        this.cryptedPIN = cryptedPIN;
    }


    public String  getInitVector() {
        return initVector;
    }

    public void setInitVector(String initVector) {
        this.initVector = initVector;
    }

}
