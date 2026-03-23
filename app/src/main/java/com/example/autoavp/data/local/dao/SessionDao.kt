package com.example.autoavp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.autoavp.data.local.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY dateCreated DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY dateCreated DESC LIMIT 1")
    fun getLatestSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE sessionId = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("DELETE FROM sessions WHERE sessionId = :id")
    suspend fun deleteSession(id: Long)
}
