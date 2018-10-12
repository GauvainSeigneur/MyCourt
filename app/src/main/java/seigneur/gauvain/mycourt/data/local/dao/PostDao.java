package seigneur.gauvain.mycourt.data.local.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import retrofit2.http.DELETE;
import seigneur.gauvain.mycourt.data.model.Draft;
import seigneur.gauvain.mycourt.data.model.ShotDraft;

@Dao
public interface PostDao {

    @Query("SELECT * FROM Draft")
    Maybe<List<Draft>> getAllPost();

    @Query("SELECT * FROM ShotDraft WHERE id IN (:postIds)")
    Flowable<List<ShotDraft>> loadAllPostByIds(int[] postIds);

    @Query("SELECT * FROM Draft WHERE id IN (:shotId)")
    Maybe<Draft> getShotDraftByShotId(String shotId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateDraft(Draft draft);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPost(Draft draft);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAllPost(List<ShotDraft> shotDraft);

    @Query("DELETE FROM shotdraft WHERE id = :id")
    int deletDraftByID(int id);

}
