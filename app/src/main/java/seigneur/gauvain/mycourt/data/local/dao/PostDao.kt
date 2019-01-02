package seigneur.gauvain.mycourt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("DELETE FROM Draft WHERE draftID = :draftID")
    fun deletDraftByID(draftID: Int): Int

}
