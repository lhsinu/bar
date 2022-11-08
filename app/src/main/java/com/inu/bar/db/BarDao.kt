package com.inu.bar.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BarDao {
    @Query("SELECT * FROM BarEntity ORDER BY accidentdate DESC")
    fun getAll() : List<BarEntity>

    @Insert
    fun insertTodo(todo : BarEntity)

    @Delete
    fun deleteTodo(todo : BarEntity)

    @Query("SELECT * FROM BarEntity WHERE driving = 1 ORDER BY accidentdate DESC LIMIT 1")
    fun getRecent() : BarEntity
}