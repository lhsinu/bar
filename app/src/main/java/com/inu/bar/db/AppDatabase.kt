package com.inu.bar.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = arrayOf(BarEntity::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getBarDao() : BarDao

    companion object {
        val databaseName = "db_bar"
        var appDatabase : AppDatabase? = null

        fun getInstance(context: Context) : AppDatabase? {
            if(appDatabase == null) {
                appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
            }
            return appDatabase
        }
    }
}