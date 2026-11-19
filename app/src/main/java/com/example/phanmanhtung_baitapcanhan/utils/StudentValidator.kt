package com.example.phanmanhtung_baitapcanhan.util

object StudentValidator {
    private val codeRegex = Regex("^[A-Z0-9]{5,15}$")
    private val nameRegex = Regex("^[\\p{L}\\p{M}]+(?:[ '\\-][\\p{L}\\p{M}]+)*$")
    private val classRegex = Regex("^[A-Z0-9][A-Z0-9.\\-]{1,19}$")

    fun codeError(value: String): String? = when {
        value.isBlank() -> "Vui lòng nhập mã sinh viên"
        !codeRegex.matches(value) -> "Mã gồm 5–15 chữ A–Z hoặc số"
        else -> null
    }

    fun nameError(value: String): String? = when {
        value.isBlank() -> "Vui lòng nhập họ tên"
        value.length !in 2..60 -> "Họ tên gồm 2–60 ký tự"
        !nameRegex.matches(value) -> "Họ tên không được chứa số hoặc ký tự lạ"
        else -> null
    }

    fun classError(value: String): String? = when {
        value.isBlank() -> "Vui lòng nhập lớp"
        !classRegex.matches(value) -> "Lớp gồm 2–20 chữ, số, dấu . hoặc -"
        else -> null
    }

    fun parseGpa(value: String): Double? = value.replace(',', '.').toDoubleOrNull()

    fun gpaError(value: String): String? {
        val number = parseGpa(value)
        return when {
            value.isBlank() -> "Vui lòng nhập GPA"
            number == null || !number.isFinite() -> "GPA phải là số hợp lệ"
            number !in 0.0..4.0 -> "GPA phải từ 0 đến 4"
            else -> null
        }
    }
}