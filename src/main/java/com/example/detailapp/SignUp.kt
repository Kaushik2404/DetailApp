package com.example.detailapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.example.detailapp.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.net.URL
import java.util.concurrent.TimeUnit

class SignUp : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    private lateinit var dbRef:DatabaseReference
    private lateinit var dbRef2:DatabaseReference
    lateinit var auth:FirebaseAuth
    var number : String =""
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    lateinit var googleSignInClient: GoogleSignInClient
   // lateinit var picURL:Uri

    var check:Boolean=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().getReference("Student")

        dbRef2 = FirebaseDatabase.getInstance().getReference("Admin")
        auth = FirebaseAuth.getInstance()

        googleSignInClient= GoogleSignIn.getClient(this@SignUp, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut().addOnCompleteListener {    task->
            if(task.isSuccessful){
                auth.signOut()
            }
        }




        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val Islogin = prefs.getBoolean("Islogin", false)
        val role = prefs.getString("ROLE", null)
        val okName=prefs.getString("NAME",null)
        val okcourse=prefs.getString("COURSE",null)
        val okemail=prefs.getString("EMAIL",null)
        val okdob=prefs.getString("DOB",null)
        val okphone=prefs.getString("PHONE",null)
        val okpass=prefs.getString("PASSWORD",null)

        val user = auth.currentUser
        if (user != null) {
            val personName = user.displayName
            val personEmail = user.email
            // val birthdate = user.vv
            // picURL = user.photoUrl!!

            binding.edEmail.setText(personEmail.toString())
            binding.edName.setText(personName.toString())
            //Glide.with(this@SignUp).load(picURL).into(binding.profileimage)
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                startActivity(Intent(applicationContext, Home::class.java))
                finish()
                Log.d("GFG", "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("GFG", "onVerificationFailed  $e")
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("GFG", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

            }
        }
        binding.sendOtp.setOnClickListener {
            binding.Otptimer.visibility = View.VISIBLE
            binding.sendOtp.isClickable = false
            object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.Otptimer.text = "00:" + millisUntilFinished / 1000
                }

                override fun onFinish() {
                    binding.Otptimer.text = "Time's finished!"
                    binding.resendOTP.visibility = View.VISIBLE
                    binding.sendOtp.isClickable = true

                }
            }.start()
            login()

        }

        binding.spinRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {


                if (binding.spinRole.selectedItem.toString() == "Admin") {


                    binding.txtCourse.visibility = View.GONE
                    binding.txtDob.visibility = View.GONE
                    binding.edCourse.visibility = View.GONE
                    binding.edDob.visibility = View.GONE

                } else if (binding.spinRole.selectedItem.toString() == "Student") {
                    binding.txtCourse.visibility = View.VISIBLE
                    binding.txtDob.visibility = View.VISIBLE

                    binding.edCourse.visibility = View.VISIBLE
                    binding.edDob.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        binding.signup.setOnClickListener {

            binding.resendOTP.visibility = View.GONE

            val otp = binding.edotp.text.toString().trim()

            if (otp.isNotEmpty()) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), otp
                )

                if (checkValidData()) {
                    if (binding.spinRole.selectedItem.toString() == "Admin") {
                        signInWithPhoneAuthCredential(
                            credential,
                            binding.spinRole.selectedItem.toString()
                        )
                    } else if (binding.spinRole.selectedItem.toString() == "Student") {

                        signInWithPhoneAuthCredential(
                            credential,
                            binding.spinRole.selectedItem.toString()
                        )

                    }


                }

//                        val intent = Intent(applicationContext, Home::class.java)
//                        startActivity(intent)
//                        finish()
            } else {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
            }


        }

    }


    private fun login() {
        number= binding.edPh.text.trim().toString()

        // get the phone number from edit text and append the country cde with it
        if (number.isNotEmpty()){
            number = "+91$number"
            sendVerificationCode(number)
        }else{
            Toast.makeText(this,"Enter mobile number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("GFG" , "Auth started")

    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential,context:String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if(context=="Admin"){
                        saveAdminData()
                        check=true
                        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                        prefs.edit().putBoolean("Islogin", check).commit()
                        prefs.edit().putString("ROLE",binding.spinRole.selectedItem.toString()).commit()
                        val intent = Intent(this , Home::class.java)
                       // intent.putExtra("PICURI",picURL)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        saveStudentData()


                        val role = binding.spinRole.selectedItem.toString()
                        val name = binding.edName.text.toString()
                        val course = binding.edCourse.text.toString()
                        val dob = binding.edDob.text.toString()
                        val email = binding.edEmail.text.toString()
                        val ph = binding.edPh.text.toString()
                        val password = binding.edPassword.text.toString()

                        check=true
                        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                        prefs.edit().putBoolean("Islogin", check).commit()
                        prefs.edit().putString("ROLE",binding.spinRole.selectedItem.toString()).commit()
                        prefs.edit().putString("NAME",name).commit()
                        prefs.edit().putString("COURSE",course).commit()
                        prefs.edit().putString("DOB",dob).commit()
                        prefs.edit().putString("EMAIL",email).commit()
                        prefs.edit().putString("PHONE",ph).commit()
                        prefs.edit().putString("PASSWORD",password).commit()


                        val intent = Intent(applicationContext, HomeStudent::class.java)
                        //intent.putExtra("PICURI",picURL)
                        intent.putExtra("NAME", name)
                        intent.putExtra("COURSE", course)
                        intent.putExtra("DOB", dob)
                        intent.putExtra("EMAIL", email)
                        intent.putExtra("PHONE", ph)
                        intent.putExtra("PASSWORD", password)
                        startActivity(intent)
                        finish()
                    }

                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this,"Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun saveAdminData() {
        val role=binding.spinRole.selectedItem.toString()
        val name=binding.edName.text.toString()
        val email=binding.edEmail.text.toString()
        val ph=binding.edPh.text.toString()
        val password=binding.edPassword.text.toString()

        val adminId=dbRef.push().key!!

        val admin=AdminModal(adminId,role,name,email,ph,password)

        dbRef2.child(adminId).setValue(admin).addOnCompleteListener {
            Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Data not Inserted", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveStudentData() {
        val role=binding.spinRole.selectedItem.toString()
        val name=binding.edName.text.toString()
        val course=binding.edCourse.text.toString()
        val dob=binding.edDob.text.toString()
        val email=binding.edEmail.text.toString()
        val password=binding.edPassword.text.toString()
        val ph=binding.edPh.text.toString()

        val studId=dbRef.push().key!!

        val stud=StudentModel(studId,role,name,course,dob,email,ph,password)

        dbRef.child(studId).setValue(stud).addOnCompleteListener {
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
//        if (binding.edCourse.text.isEmpty()){
//            binding.edCourse.error="enter Corse Detail"
//            return false
//        }
//        if (binding.edDob.text.isEmpty()){
//            binding.edDob.error="enter DOB"
//            return false
//        }

//        if (binding.edAddress.text.isEmpty()){
//            binding.edAddress.error="enter Address"
//            return false
//        }

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