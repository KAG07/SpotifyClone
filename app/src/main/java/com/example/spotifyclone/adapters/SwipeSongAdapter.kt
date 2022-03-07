package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.list_item.view.tvPrimary
import kotlinx.android.synthetic.main.swipe_item.view.*
import javax.inject.Inject

class SwipeSongAdapter:BaseSongAdapter(R.layout.swipe_item){

    override val differ=AsyncListDiffer<Song>(this,diffcallback)

    override fun onBindViewHolder(holder:SongViewholder, position: Int) {
        val song=songs[position]
        holder.itemView.apply {
            val text1="${song.title}-${song.Subtitle}"
            tvPrimary.text=text1
            setOnClickListener {
                onItemClickListener?.let {
                    it(song)
                }
            }
        }
    }

}