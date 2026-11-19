package com.example.phanmanhtung_baitapcanhan

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phanmanhtung_baitapcanhan.database.entity.StudentEntity
import com.example.phanmanhtung_baitapcanhan.databinding.ActivityMainBinding
import com.example.phanmanhtung_baitapcanhan.databinding.DialogStudentBinding
import com.example.phanmanhtung_baitapcanhan.ui.StudentAdapter
import com.example.phanmanhtung_baitapcanhan.util.StudentValidator
import com.example.phanmanhtung_baitapcanhan.viewmodel.StudentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: StudentViewModel by viewModels()
    private lateinit var studentAdapter: StudentAdapter
    private var editorDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()
        studentAdapter = StudentAdapter(
            onEdit = { showStudentDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = studentAdapter
        binding.edtSearch.setText(viewModel.keyword.value)
        binding.edtSearch.doAfterTextChanged {
            viewModel.setKeyword(it?.toString().orEmpty())
        }
        binding.btnAdd.setOnClickListener { showStudentDialog() }
        observeData()
    }

    private fun setupInsets() {
        val left = binding.root.paddingLeft
        val top = binding.root.paddingTop
        val right = binding.root.paddingRight
        val bottom = binding.root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(left + bars.left, top + bars.top,
                right + bars.right, bottom + maxOf(bars.bottom, ime.bottom))
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        studentAdapter.submitList(state.students)
                        binding.progress.isVisible = state.loading
                        binding.tvEmpty.isVisible = !state.loading && state.students.isEmpty()
                        binding.tvStats.text = String.format(
                            Locale.US, "%d sinh viên  •  GPA TB %.2f", state.total, state.average
                        )
                        binding.tvResultCount.text = "${state.students.size} kết quả"
                        binding.tvEmpty.text = if (viewModel.keyword.value.isBlank())
                            "Chưa có sinh viên\nNhấn nút bên dưới để bắt đầu"
                        else "Không tìm thấy sinh viên\nHãy thử tên, mã hoặc lớp khác"
                    }
                }
                launch {
                    viewModel.messages.collect { showMessage(it) }
                }
            }
        }
    }

    private fun showStudentDialog(student: StudentEntity? = null) {
        if (editorDialog?.isShowing == true) return
        val form = DialogStudentBinding.inflate(layoutInflater)
        student?.let {
            form.edtCode.setText(it.studentCode)
            form.edtName.setText(it.fullName)
            form.edtClass.setText(it.className)
            form.edtGpa.setText(it.gpa.toString())
        }
        fun code() = form.edtCode.text.toString().trim().uppercase(Locale.ROOT)
        fun name() = form.edtName.text.toString().trim().replace(Regex("\\s+"), " ")
        fun className() = form.edtClass.text.toString().trim().uppercase(Locale.ROOT)
        fun gpa() = form.edtGpa.text.toString().trim()

        form.edtCode.doAfterTextChanged { form.tilCode.error = StudentValidator.codeError(code()) }
        form.edtName.doAfterTextChanged { form.tilName.error = StudentValidator.nameError(name()) }
        form.edtClass.doAfterTextChanged { form.tilClass.error = StudentValidator.classError(className()) }
        form.edtGpa.doAfterTextChanged { form.tilGpa.error = StudentValidator.gpaError(gpa()) }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (student == null) "Thêm sinh viên" else "Chỉnh sửa sinh viên")
            .setView(form.root)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu", null)
            .create()
        editorDialog = dialog
        dialog.setOnDismissListener { editorDialog = null }
        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            saveButton.setOnClickListener {
                val codeError = StudentValidator.codeError(code())
                val nameError = StudentValidator.nameError(name())
                val classError = StudentValidator.classError(className())
                val gpaError = StudentValidator.gpaError(gpa())
                form.tilCode.error = codeError
                form.tilName.error = nameError
                form.tilClass.error = classError
                form.tilGpa.error = gpaError
                if (listOf(codeError, nameError, classError, gpaError).any { it != null }) {
                    return@setOnClickListener
                }
                val entity = StudentEntity(
                    id = student?.id ?: 0,
                    studentCode = code(), fullName = name(), className = className(),
                    gpa = StudentValidator.parseGpa(gpa())!!,
                    updatedAt = System.currentTimeMillis()
                )
                saveButton.isEnabled = false
                cancelButton.isEnabled = false
                listOf(form.edtCode, form.edtName, form.edtClass, form.edtGpa)
                    .forEach { it.isEnabled = false }
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                lifecycleScope.launch {
                    val error = viewModel.save(entity)
                    if (isFinishing || isDestroyed) return@launch
                    if (error == null) {
                        dialog.dismiss()
                        showMessage(if (student == null) "Đã thêm sinh viên" else "Đã cập nhật sinh viên")
                    } else {
                        saveButton.isEnabled = true
                        cancelButton.isEnabled = true
                        listOf(form.edtCode, form.edtName, form.edtClass, form.edtGpa)
                            .forEach { it.isEnabled = true }
                        dialog.setCancelable(true)
                        dialog.setCanceledOnTouchOutside(true)
                        if (error == "Mã sinh viên đã tồn tại") {
                            form.tilCode.error = error
                            form.edtCode.requestFocus()
                        } else {
                            MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle("Chưa lưu được").setMessage(error)
                                .setPositiveButton("Đã hiểu", null).show()
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(student: StudentEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xóa sinh viên?")
            .setMessage("Xóa ${student.fullName} (${student.studentCode})?\nKhông thể hoàn tác thao tác này.")
            .setNegativeButton("Giữ lại", null)
            .setPositiveButton("Xóa") { _, _ -> viewModel.delete(student) }
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        editorDialog?.dismiss()
        super.onDestroy()
    }
}