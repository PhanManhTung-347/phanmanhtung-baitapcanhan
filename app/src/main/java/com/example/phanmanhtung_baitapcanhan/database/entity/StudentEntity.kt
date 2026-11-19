package com.example.phanmanhtung_baitapcanhan.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "student", indices = [Index(value = ["student_code"], unique = true)])
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "student_code") val studentCode: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "class_name") val className: String,
    val gpa: Double,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)