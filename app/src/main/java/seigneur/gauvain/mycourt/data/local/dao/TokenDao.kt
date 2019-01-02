package seigneur.gauvain.mycourt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import io.reactivex.Maybe
import seigneur.gauvain.mycourt.data.model.Token

@Dao
interface TokenDao {

    @get:Query("SELECT * FROM token")
    val token: Maybe<Token>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToken(token: Token): Long

}
