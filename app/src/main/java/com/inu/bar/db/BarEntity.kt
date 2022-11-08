package com.inu.bar.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BarEntity(
    @PrimaryKey(autoGenerate = true) var id : Int? = null,
    @ColumnInfo(name="accidentdate") val accidentdate : Long,
    @ColumnInfo(name="driving") val driving : Boolean,
//    @ColumnInfo(name="heart") val heart : Boolean,
    @ColumnInfo(name="camfront") val camfront : String,
    @ColumnInfo(name="camback") val camback : String,
    @ColumnInfo(name="gps") val gps : String
)
