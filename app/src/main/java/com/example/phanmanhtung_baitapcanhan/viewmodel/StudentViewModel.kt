package com.example.phanmanhtung_baitapcanhan.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.phanmanhtung_baitapcanhan.database.AppDb
import com.example.phanmanhtung_baitapcanhan.database.entity.StudentEntity
import com.example.phanmanhtung_baitapcanhan.repository.StudentRepository
import com.example.phanmanhtung_baitapcanhan.util.StudentValidator
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StudentUiState(
    val students: List<StudentEntity> = emptyList(),
    val total: Int = 0,
    val average: Double = 0.0,
    val loading: Boolean = true
)

class StudentViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val repository = StudentRepository(AppDb(application).studentDao())
    val keyword = savedStateHandle.getStateFlow("keyword", "")
    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    private val students = repository.students.catch { error ->
        if (error is CancellationException) throw error
        messageChannel.send("Không thể đọc dữ liệu. Hãy mở lại ứng dụng")
        emit(emptyList())
    }

    val uiState = combine(students, keyword) { all, query ->
        val search = normalize(query.trim())
        val result = if (search.isBlank()) all else all.filter {
            normalize("${it.studentCode} ${it.fullName} ${it.className}").contains(search)
        }
        StudentUiState(
            students = result,
            total = all.size,
            average = if (all.isEmpty()) 0.0 else all.map { it.gpa }.average(),
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentUiState())

    fun setKeyword(value: String) { savedStateHandle["keyword"] = value }

    suspend fun save(student: StudentEntity): String? {
        val validationError = listOfNotNull(
            StudentValidator.codeError(student.studentCode),
            StudentValidator.nameError(student.fullName),
            StudentValidator.classError(student.className),
            StudentValidator.gpaError(student.gpa.toString())
        ).firstOrNull()
        if (validationError != null) return validationError

        return try {
            if (repository.existsCode(student.studentCode, student.id)) {
                "Mã sinh viên đã tồn tại"
            } else {
                repository.save(student)
                null
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: SQLiteConstraintException) {
            "Mã sinh viên đã tồn tại"
        } catch (error: Exception) {
            "Không thể lưu. Vui lòng thử lại"
        }
    }

    fun delete(student: StudentEntity) {
        viewModelScope.launch {
            try {
                repository.delete(student)
                messageChannel.send("Đã xóa ${student.fullName}")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                messageChannel.send("Không thể xóa sinh viên")
            }
        }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace('đ', 'd').replace('Đ', 'D')
            .lowercase(Locale.ROOT)
}