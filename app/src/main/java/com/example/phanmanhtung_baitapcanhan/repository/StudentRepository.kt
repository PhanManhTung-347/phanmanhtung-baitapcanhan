package com.example.phanmanhtung_baitapcanhan.repository

import com.example.phanmanhtung_baitapcanhan.database.dao.StudentDao
import com.example.phanmanhtung_baitapcanhan.database.entity.StudentEntity

class StudentRepository(private val dao: StudentDao) {
    val students = dao.observeStudents()

    suspend fun existsCode(code: String, excludedId: Int) =
        dao.existsCode(code, excludedId)

    suspend fun save(student: StudentEntity) {
        if (student.id == 0) dao.insertStudent(student)
        else dao.updateStudent(student)
    }

    suspend fun delete(student: StudentEntity) = dao.deleteStudent(student)
}