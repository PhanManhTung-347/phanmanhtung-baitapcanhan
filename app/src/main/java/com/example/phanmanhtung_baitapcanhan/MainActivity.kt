package com.example.phanmanhtung_baitapcanhan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.phanmanhtung_baitapcanhan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progress.visibility = android.view.View.GONE
        binding.tvEmpty.visibility = android.view.View.VISIBLE
    }
}