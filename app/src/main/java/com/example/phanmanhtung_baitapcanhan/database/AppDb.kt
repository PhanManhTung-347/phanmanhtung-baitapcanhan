package com.example.phanmanhtung_baitapcanhan.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.phanmanhtung_baitapcanhan.database.dao.StudentDao
import com.example.phanmanhtung_baitapcanhan.database.entity.StudentEntity

@Database(entities = [StudentEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile private var instance: AppDb? = null

        operator fun invoke(context: Context): AppDb =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    "student_leaf.db"
                ).build().also { instance = it }
            }
    }
}