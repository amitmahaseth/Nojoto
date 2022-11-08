package com.example.nojoto.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nojoto.R

class UserAdapter(var mContext:Context): RecyclerView.Adapter<UserDataViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDataViewHolder {
        val view=LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false)
        return UserDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserDataViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 10
    }
}

class UserDataViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {

}
