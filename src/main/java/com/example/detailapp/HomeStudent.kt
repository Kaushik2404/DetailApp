package com.example.detailapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.example.detailapp.databinding.ActivityHomeStudentBinding


class HomeStudent : AppCompatActivity() {
   private lateinit var binding: ActivityHomeStudentBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding=ActivityHomeStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

//     Glide.with(this@HomeStudent).load(intent.getStringExtra("PICURI")).into(binding.profileimage)

        binding.logout.setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit().putBoolean("Islogin", false).commit()
            val intent =Intent(applicationContext,Login::class.java)
            startActivity(intent)
            finish()

        }



//        var id:String=intent.getStringExtra("ID").toString()
//        var role:String=intent.getStringExtra("ROLE").toString()
        binding.chName.text=intent.getStringExtra("NAME")
        binding.chCourse.text=intent.getStringExtra("COURSE")
        binding.chDob.text=intent.getStringExtra("DOB")
        binding.chEmail.text=intent.getStringExtra("EMAIL")
        binding.chPhone.text=intent.getStringExtra("PHONE")
        binding.chPass.text=intent.getStringExtra("PASSWORD")
    }
}