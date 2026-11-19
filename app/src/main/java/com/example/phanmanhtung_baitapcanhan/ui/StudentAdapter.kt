package com.example.phanmanhtung_baitapcanhan.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.phanmanhtung_baitapcanhan.database.entity.StudentEntity
import com.example.phanmanhtung_baitapcanhan.databinding.ItemStudentBinding
import java.util.Locale

class StudentAdapter(
    private val onEdit: (StudentEntity) -> Unit,
    private val onDelete: (StudentEntity) -> Unit
) : ListAdapter<StudentEntity, StudentAdapter.Holder>(Diff) {
    class Holder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val student = getItem(position)
        holder.binding.apply {
            tvAvatar.text = student.fullName.split(Regex("\\s+"))
                .filter { it.isNotBlank() }.takeLast(2)
                .joinToString("") { it.take(1) }.uppercase(Locale.ROOT)
            tvFullName.text = student.fullName
            tvStudentCode.text = "MSV · ${student.studentCode}"
            tvDetails.text = String.format(
                Locale.US, "Lớp %s   •   GPA %.2f / 4", student.className, student.gpa
            )
            tvRank.text = when {
                student.gpa >= 3.6 -> "★ Xuất sắc"
                student.gpa >= 3.2 -> "★ Giỏi"
                student.gpa >= 2.5 -> "● Khá"
                student.gpa >= 2.0 -> "● Trung bình"
                else -> "● Cần cố gắng"
            }
            btnEdit.setOnClickListener { onEdit(student) }
            btnDelete.setOnClickListener { onDelete(student) }
        }
    }

    companion object {
        private val Diff = object : DiffUtil.ItemCallback<StudentEntity>() {
            override fun areItemsTheSame(oldItem: StudentEntity, newItem: StudentEntity) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: StudentEntity, newItem: StudentEntity) =
                oldItem == newItem
        }
    }
}