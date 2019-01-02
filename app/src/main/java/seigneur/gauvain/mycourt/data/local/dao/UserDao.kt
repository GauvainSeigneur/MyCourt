package seigneur.gauvain.mycourt.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
