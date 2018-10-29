package seigneur.gauvain.mycourt.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Maybe
import seigneur.gauvain.mycourt.data.model.Draft

@Dao
interface PostDao {

    @get:Query("SELECT * FROM Draft")
    val allPost: Maybe<List<Draft>>

    @Query("SELECT * FROM Draft WHERE id IN (:shotId)")
    fun getShotDraftByShotId(shotId: String): Maybe<Draft>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDraft(draft: Draft): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(draft: Draft): Long

    @Query("DELETE FROM draft WHERE id = :id")
    fun deletDraftByID(id: Int): Int

}
