package seigneur.gauvain.mycourt.data.local.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import seigneur.gauvain.mycourt.data.model.User;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);

    @Query("SELECT * FROM user")
    Maybe<User> getUser();

    @Query("SELECT * FROM user WHERE isAllowedToUpload")
    boolean checkIfUserIsAllowedToUpload();

    @Delete
    void deleteUser(User user);

}
