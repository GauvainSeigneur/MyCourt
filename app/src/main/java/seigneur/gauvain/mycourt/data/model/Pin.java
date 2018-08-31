package seigneur.gauvain.mycourt.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

//insert relation : https://stackoverflow.com/questions/44667160/android-room-insert-relation-entities-using-room
@Entity
public class Pin {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public byte[] cryptedPIN;
    public byte[] initVector;

    public Pin(int id, byte[] cryptedPIN, byte[] initVector) {
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

    public byte[] getCryptedPIN() {
        return cryptedPIN;
    }

    public void setCryptedPIN(byte[] cryptedPIN) {
        this.cryptedPIN = cryptedPIN;
    }


    public byte[]  getInitVector() {
        return initVector;
    }

    public void setInitVector(byte[] initVector) {
        this.initVector = initVector;
    }

}
