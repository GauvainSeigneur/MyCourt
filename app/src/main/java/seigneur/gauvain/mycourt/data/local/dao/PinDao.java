package seigneur.gauvain.mycourt.data.local.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import io.reactivex.Maybe;
import seigneur.gauvain.mycourt.data.model.Pin;

@Dao
public interface PinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPIN(Pin pin);

    @Query("SELECT * FROM pin")
    Maybe<Pin> getPin();

    @Query("UPDATE pin SET cryptedPIN = :pin")
    void updateCryptedPwd(String pin);


    @Delete
    void deletePin(Pin pin);

}
