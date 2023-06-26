package com.example.detailapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.example.detailapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private lateinit var studList : ArrayList<StudentModel>
    private lateinit var adList : ArrayList<AdminModal>
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef2: DatabaseReference
    var isCheckOK:Boolean=true

    var auth=FirebaseAuth.getInstance()

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    lateinit var gso:GoogleSignInOptions
    private val RC_SIGNIN:Int=100

    var check:Boolean=false



    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val Islogin = prefs.getBoolean("Islogin", false)
        val role = prefs.getString("ROLE", null)
        val okName=prefs.getString("NAME",null)
        val okcourse=prefs.getString("COURSE",null)
        val okemail=prefs.getString("EMAIL",null)
        val okphone=prefs.getString("PHONE",null)
        val okdob=prefs.getString("DOB",null)
        val okpass=prefs.getString("PASSWORD",null)


        studList= arrayListOf()
        adList= arrayListOf()


            if(Islogin){
                if(role=="Student"){
                    val intent=Intent(applicationContext,HomeStudent::class.java)
                    intent.putExtra("NAME",okName)
                    intent.putExtra("COURSE",okcourse)
                    intent.putExtra("EMAIL",okemail)
                    intent.putExtra("DOB",okdob)
                    intent.putExtra("PHONE",okphone)
                    intent.putExtra("PASSWORD",okpass)

                    startActivity(intent)

                    finish()
                }
                else if(role=="Admin"){
                    val intent=Intent(applicationContext,Home::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT).show()
                }

            }



         gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
             .requestIdToken("292544379793-lh9da0d5o9t6ts1ngilp1rb5sotr9hjr.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        val account = GoogleSignIn.getLastSignedInAccount(this)

        binding.google.setOnClickListener{
            signIn()
        }





        binding.login.setOnClickListener{
                if(checkField()){
                    if(binding.spinRole.selectedItem.toString()=="Admin"){
                        getCheckaddminData()



                    }
                    else if(binding.spinRole.selectedItem.toString()=="Student"){

                        getCheckStudData()

                    }
                }
            }
        binding.signUp.setOnClickListener{
            val intent=Intent(applicationContext,SignUp::class.java)
            startActivity(intent)
        }

    }

    private fun signIn() {
        val signinIntent:Intent=mGoogleSignInClient.signInIntent
        startActivityForResult(signinIntent,RC_SIGNIN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==RC_SIGNIN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            if(task!=null){
            try{
                val account=task.getResult(ApiException::class.java)!!
                // Log.d(Companion.TAG,"firebaseAuthWithGoogle:"+account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e:ApiException){
                // Log.w(Companion.TAG,"Google sign in failed",e)
                Toast.makeText(applicationContext, "errorr", Toast.LENGTH_SHORT).show()
            }
//            }else  {
//                Toast.makeText(applicationContext, "errorrrr", Toast.LENGTH_SHORT).show()
//            }
            //handleSignInResult(task)

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential= GoogleAuthProvider.getCredential(idToken,null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task->
                if(task.isSuccessful){
                    Log.d("TAG","signInWithCredential:success")
                    val user=auth.currentUser
                    val intent=Intent(applicationContext,SignUp::class.java)
                    startActivity(intent)
                    finish()

                } else{
                    Log.w("TAG","signInWithCredential:failure",task.exception)
                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
                }

            }

    }
    companion object {
        private const val TAG = "GoogleActivity"
    }

    private fun getCheckaddminData() {
        dbRef= FirebaseDatabase.getInstance().getReference("Admin")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adList.clear()
                if (snapshot.exists()){
                    for (addSnap in snapshot.children){
                        val addData=addSnap.getValue(AdminModal::class.java)
                        adList.add(addData!!)


                        if(addData.email==binding.edUserName.text.toString()
                            && addData.pass==binding.edPassword.text.toString()
                            &&"Admin"==binding.spinRole.selectedItem.toString()
                        ){
                            check=true
                            isCheckOK=false
                            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                            prefs.edit().putBoolean("Islogin", check).commit()
                            prefs.edit().putString("ROLE",binding.spinRole.selectedItem.toString()).commit()

                            val intent=Intent(applicationContext,Home::class.java)
                            startActivity(intent)
                            finish()
                        }

//                        else if(addData.userName!=binding.edUserName.text.toString() &&
//                            addData.pass!=binding.edPassword.text.toString()){
//                            isCheckOK=true
//                        }

                    }
                    if(isCheckOK){
                        Toast.makeText(this@Login, "error", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getCheckStudData() {


        dbRef2= FirebaseDatabase.getInstance().getReference("Student")

        dbRef2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studList.clear()
                if (snapshot.exists()){
                    for (studSnap in snapshot.children){
                        val studData=studSnap.getValue(StudentModel::class.java)
                        studList.add(studData!!)



                        if (studData.email==binding.edUserName.text.toString()&&
                            studData.password==binding.edPassword.text.toString()&&
                            "Student"==binding.spinRole.selectedItem.toString()&&
                            studData.role==binding.spinRole.selectedItem.toString()){


                            check=true
                            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                            prefs.edit().putBoolean("Islogin", check).commit()
                            prefs.edit().putString("ROLE",binding.spinRole.selectedItem.toString()).commit()
                            prefs.edit().putString("NAME",studData.namee).commit()
                            prefs.edit().putString("COURSE",studData.course).commit()
                            prefs.edit().putString("DOB",studData.dob).commit()
                            prefs.edit().putString("EMAIL",studData.email).commit()
                            prefs.edit().putString("PHONE",studData.ph).commit()
                            prefs.edit().putString("PASSWORD",studData.password).commit()


                            isCheckOK=false

                            val id=studData.studID
                            val role=studData.role
                            val name=studData.namee
                            val course=studData.course
                            val dob=studData.dob
                            val email=studData.email
                            val phone=studData.ph
                            val password=studData.password



                            val intent=Intent(applicationContext,HomeStudent::class.java)
                            intent.putExtra("ID",id)
                            intent.putExtra("ROLE",role)
                            intent.putExtra("NAME",name)
                            intent.putExtra("COURSE",course)
                            intent.putExtra("DOB",dob)
                            intent.putExtra("EMAIL",email)
                            intent.putExtra("PHONE",phone)
                            intent.putExtra("PASSWORD",password)
                            startActivity(intent)
                            finish()
                        }
                        else if(studData.email!=binding.edUserName.text.toString() &&
                            studData.password!=binding.edPassword.text.toString()){
                            isCheckOK=false
                        }

                    }
                    if(isCheckOK){
                        Toast.makeText(this@Login, "error", Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    private fun checkField(): Boolean {
        if (binding.spinRole.selectedItem.equals("Select the Role :")){
            Toast.makeText(this, " Please Select the Role ", Toast.LENGTH_SHORT).show()
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

        return true
    }
}