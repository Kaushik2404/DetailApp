package com.example.detailapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.detailapp.databinding.ActivityUpdateBinding
import com.google.firebase.database.FirebaseDatabase

class Update : AppCompatActivity() {
   private lateinit var binding: ActivityUpdateBinding
   private lateinit var id:String
   private lateinit var roleOk:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        id= intent.getStringExtra("ID").toString()
        roleOk= intent.getStringExtra("ROLE").toString()
        binding.edName.setText(intent.getStringExtra("NAME"))
        binding.edCourse.setText(intent.getStringExtra("COURSE"))
        binding.edDob.setText(intent.getStringExtra("DOB"))
        binding.edEmail.setText(intent.getStringExtra("EMAIL"))
        binding.edPh.setText(intent.getStringExtra("PHONE"))
        binding.edPassword.setText(intent.getStringExtra("PASSWORD"))






        updateData()
        binding.btnUpdate.setOnClickListener {
          val builder=AlertDialog.Builder(this@Update)
            builder.setCancelable(false)
            builder.setIcon(R.drawable.baseline_edit_24)
            builder.setTitle("Update Data !")
            builder.setMessage("Are you Confirm Changes this Data....")
            builder.setPositiveButton("yes"){dialog, which->

                val name=binding.edName.text.toString()
                val course=binding.edCourse.text.toString()
                val dob=binding.edDob.text.toString()
                val email=binding.edEmail.text.toString()
                val ph=binding.edPh.text.toString()
                val password=binding.edPassword.text.toString()

                val dbRef=FirebaseDatabase.getInstance().getReference("Student").child(id)
                val stud=StudentModel(id,roleOk,name,course,dob,email,ph,password)
                dbRef.setValue(stud).addOnCompleteListener {
                    Toast.makeText(this, "update", Toast.LENGTH_SHORT).show()
                    val intent=Intent(applicationContext,Home::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "fail update", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("NO"){dialog,which->
                Toast.makeText(applicationContext, "cancel Update data", Toast.LENGTH_SHORT).show()
                val intent=Intent(applicationContext,Home::class.java)
                startActivity(intent)
                finish()

            }
            val dialog=builder.create()
            dialog.show()

        }

    }

    private fun updateData() {

    }
}