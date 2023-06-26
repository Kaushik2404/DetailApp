package com.example.detailapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudAdapter(private val studList:ArrayList<StudentModel>, private val context: Home,val onIteamClick:OnIteamClick):RecyclerView.Adapter<StudAdapter.ViewHolder>() {
    private lateinit var mListener: onIteamCLickLister
    interface onIteamCLickLister{
        fun onIteamCLick(position: Int)
    }

    fun setOnIteamCLickListener(clickListener: onIteamCLickLister){
        mListener=clickListener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudAdapter.ViewHolder {
        val iteamView=LayoutInflater.from(parent.context).inflate(R.layout.list_stud,parent,false)
        return ViewHolder(iteamView,mListener)

    }

    override fun onBindViewHolder(holder: StudAdapter.ViewHolder, position: Int) {
        holder.rvText.text=studList[position].namee
        holder.clickUpdate.setOnClickListener {
            onIteamClick.onOptionMenu(position)
        }
    }

    override fun getItemCount(): Int {
       return studList.size
    }



    class ViewHolder(iteamView: View,clickListener: onIteamCLickLister) :RecyclerView.ViewHolder(iteamView) {
        val rvText: TextView=iteamView.findViewById(R.id.rvText)
        val clickUpdate: ImageView=iteamView.findViewById(R.id.clickUpdate)
//        init {
//            iteamView.setOnClickListener {
//                clickListener.onIteamCLick(adapterPosition)
//            }
//
//        }

    }

}