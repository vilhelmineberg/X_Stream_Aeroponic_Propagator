package se.vilhelmineberg.x_streamaeroponicpropagator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BoxDao {
    @Query("SELECT * FROM boxes ORDER BY createdAtEpochDay, id")
    fun boxes(): Flow<List<Box>>

    @Insert
    suspend fun insert(box: Box): Long

    @Query("DELETE FROM boxes WHERE id = :boxId")
    suspend fun delete(boxId: Long)
}

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants WHERE boxId = :boxId ORDER BY position")
    fun plantsForBox(boxId: Long): Flow<List<Plant>>

    @Query("SELECT DISTINCT name FROM plants ORDER BY name COLLATE NOCASE")
    fun allPlantNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(plants: List<Plant>)

    @Query("UPDATE plants SET name = :name WHERE id = :plantId")
    suspend fun rename(plantId: Long, name: String)

    @Query("DELETE FROM plants WHERE id = :plantId")
    suspend fun delete(plantId: Long)

    @Query("DELETE FROM plants WHERE boxId = :boxId AND position IN (:positions)")
    suspend fun deleteAtPositions(boxId: Long, positions: List<Int>)
}
