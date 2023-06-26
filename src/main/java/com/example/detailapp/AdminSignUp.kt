package com.example.detailapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.detailapp.databinding.ActivityAdminSignUpBinding

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class AdminSignUp : AppCompatActivity() {

     private lateinit var binding: ActivityAdminSignUpBinding
     private lateinit  var dbRef:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAdminSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef= FirebaseDatabase.getInstance().getReference("Admin")

        binding.signup.setOnClickListener{

            if(checkValidData()) {
                saveAdminData()
               val intent = Intent(applicationContext, Home::class.java)

                startActivity(intent)
                finish()
            }

        }



    }

    private fun saveAdminData() {
        val role=binding.spinRole.selectedItem.toString()
        val name=binding.edName.text.toString()
        val lastName=binding.edLastName.text.toString()
        val userName=binding.edUserName.text.toString()
        val password=binding.edPassword.text.toString()

        val adminId=dbRef.push().key!!

        val admin=AdminModal(adminId,role,name,userName,password)

        dbRef.child(adminId).setValue(admin).addOnCompleteListener {
            Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Data not Inserted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkValidData(): Boolean {
        if (binding.spinRole.selectedItem.equals("Select the Role :")){
            Toast.makeText(this, " Please Select the Role ", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.edName.text.isEmpty()){
            binding.edName.error="enter Name"
            return false
        }

        if (binding.edUserName.text.isEmpty()){
            binding.edUserName.error="enter User Name"
            return false
        }

        if (binding.edPassword.text.isEmpty()){
            binding.edPassword.error="enter password"
            return false
        }
        if (binding.edConfirmPassword.text.isEmpty()){
            binding.edConfirmPassword.error="enter Confirm Password"
            return false
        }
        if (binding.edPassword.text.toString()!=binding.edConfirmPassword.text.toString()){
            Toast.makeText(this, "enter same password or confirm password", Toast.LENGTH_SHORT).show()
            return false
        }

        return true

    }
}