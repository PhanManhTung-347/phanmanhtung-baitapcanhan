package com.example.phanmanhtung_baitapcanhan.database.dao

import androidx.room.*
import com.example.phanmanhtung_baitapcanhan.database.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM student ORDER BY updated_at DESC, id DESC")
    fun observeStudents(): Flow<List<StudentEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM student WHERE student_code = :code AND id != :excludedId)")
    suspend fun existsCode(code: String, excludedId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)
}