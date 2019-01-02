package seigneur.gauvain.mycourt.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import io.reactivex.Maybe
import seigneur.gauvain.mycourt.data.model.Pin

@Dao
interface PinDao {

    @get:Query("SELECT * FROM pin")
    val pin: Maybe<Pin>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPIN(pin: Pin): Long

    @Query("UPDATE pin SET cryptedPIN = :pin")
    fun updateCryptedPwd(pin: String)

    @Delete
    fun deletePin(pin: Pin)

}
