package seigneur.gauvain.mycourt.data.local.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Maybe
import seigneur.gauvain.mycourt.data.model.User

@Dao
interface UserDao {

    @get:Query("SELECT * FROM user")
    val user: Maybe<User>

    //for testing
    @get:Query("SELECT * FROM user")
    val userLive: LiveData<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Long

    @Query("SELECT * FROM user WHERE isAllowedToUpload")
    fun checkIfUserIsAllowedToUpload(): Boolean

    @Delete
    fun deleteUser(user: User)

}
