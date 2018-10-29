package seigneur.gauvain.mycourt.data.local.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
import io.reactivex.Maybe;
import seigneur.gauvain.mycourt.data.model.Draft;

@Dao
public interface PostDao {

    @Query("SELECT * FROM Draft")
    Maybe<List<Draft>> getAllPost();


    @Query("SELECT * FROM Draft WHERE id IN (:shotId)")
    Maybe<Draft> getShotDraftByShotId(String shotId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateDraft(Draft draft);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPost(Draft draft);

    @Query("DELETE FROM draft WHERE id = :id")
    int deletDraftByID(int id);

}
