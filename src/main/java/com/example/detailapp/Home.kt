package com.example.detailapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.detailapp.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class Home : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding
    private lateinit var studList : ArrayList<StudentModel>
    private lateinit var dbRef:DatabaseReference
    lateinit var auth:FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var mAdapter:StudAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rview.layoutManager=LinearLayoutManager(this)
        binding.rview.setHasFixedSize(true)

//        Glide.with(this@Home).load(intent.getStringExtra("PICURI")).into(binding.profileimage)

        googleSignInClient= GoogleSignIn.getClient(this@Home, GoogleSignInOptions.DEFAULT_SIGN_IN)
        auth=FirebaseAuth.getInstance()
        binding.logout.setOnClickListener {

//            googleSignInClient.signOut().addOnCompleteListener {    task->
//                if(task.isSuccessful){
             val check =false
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            prefs.edit().putBoolean("Islogin", check).commit()
                    auth.signOut()
                    Toast.makeText(applicationContext, "logout successfully", Toast.LENGTH_SHORT).show()
                    val intent=Intent(applicationContext,Login::class.java)
                    startActivity(intent)
                    finish()
//                }
//            }
        }

//        binding.logout.setOnClickListener {
//
//            auth=FirebaseAuth.getInstance()
//            auth.signOut()
//            val intent=Intent(applicationContext,Login::class.java)
//            startActivity(intent)
//            finish()
//        }

        studList= arrayListOf()

        getStudData()

    }



    private fun getStudData() {
        dbRef=FirebaseDatabase.getInstance().getReference("Student")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                studList.clear()
                if (snapshot.exists()){
                    for (studSnap in snapshot.children){
                        val studData=studSnap.getValue(StudentModel::class.java)
                        studList.add(studData!!)

                    }
                     mAdapter=StudAdapter(studList,this@Home,object : OnIteamClick{
                        override fun onOptionMenu(pos: Int) {
                            performOptionsMenuClick(pos)
                        }


                    })
                    mAdapter.setOnIteamCLickListener(object:StudAdapter.onIteamCLickLister{
                        override fun onIteamCLick(position: Int) {
                            val intent=Intent(this@Home,HomeStudent::class.java)
                            intent.putExtra("NAME",studList[position].namee)
                            intent.putExtra("COURSE",studList[position].course)
                            intent.putExtra("DOB",studList[position].dob)
                            intent.putExtra("EMAIL",studList[position].email)
                            intent.putExtra("PHONE",studList[position].ph)
                            intent.putExtra("PASSWORD",studList[position].password)
                            startActivity(intent)
                        }

                    })
                    binding.rview.adapter=mAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
//    binding.rview[pos].findViewById(R.id.clickUpdate)
    private fun performOptionsMenuClick(pos: Int) {
        val popupMenu = PopupMenu(this,binding.rview[pos].findViewById(R.id.clickUpdate))
//         add the menu
        popupMenu.inflate(R.menu.options_menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }
        // implement on menu item click Listener
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when(item?.itemId){
                    R.id.Delete -> {

                        val builder= AlertDialog.Builder(this@Home)
                        builder.setCancelable(true)
                        builder.setIcon(R.drawable.baseline_edit_24)
                        builder.setTitle("Delete Data !")
                        builder.setMessage("Are you Confirm Delete this Data....")
                        builder.setPositiveButton("yes"){dialog, which->
                            dbRef = FirebaseDatabase.getInstance().getReference("Student")
                                .child(studList[pos].studID.toString())
                            dbRef.removeValue();
                            // here are the logic to delete an item from the list
                            val tempLang = studList[pos]
                            studList.remove(tempLang)
                            mAdapter.notifyItemRemoved(pos)
                        }
                        builder.setNegativeButton("NO"){dialog,which->
                            dialog.dismiss()
                        }
                        val dialog=builder.create()
                        dialog.show()


                        return true
                    }
                    // in the same way you can implement others
                    R.id.Update -> {
                        val intent=Intent(this@Home,Update::class.java)
                        intent.putExtra("ID",studList[pos].studID)
                        intent.putExtra("ROLE",studList[pos].role)
                        intent.putExtra("NAME",studList[pos].namee)
                        intent.putExtra("COURSE",studList[pos].course)
                        intent.putExtra("DOB",studList[pos].dob)
                        intent.putExtra("EMAIL",studList[pos].email)
                        intent.putExtra("PHONE",studList[pos].ph)
                        intent.putExtra("PASSWORD",studList[pos].password)
                        startActivity(intent)
                        return true
                    }
                    R.id.view -> {
                        // define
                        val intent=Intent(this@Home,HomeStudent::class.java)
                        intent.putExtra("NAME",studList[pos].namee)
                        intent.putExtra("COURSE",studList[pos].course)
                        intent.putExtra("DOB",studList[pos].dob)
                        intent.putExtra("EMAIL",studList[pos].email)
                        intent.putExtra("PHONE",studList[pos].ph)
                        intent.putExtra("PASSWORD",studList[pos].password)
                        startActivity(intent)
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()

    }
}