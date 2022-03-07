package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.*

abstract class BaseSongAdapter(private val layoutid:Int): RecyclerView.Adapter<BaseSongAdapter.SongViewholder>(){

    class SongViewholder(itemview: View): RecyclerView.ViewHolder(itemview)


    protected val diffcallback=object : DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId==newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }
    }

    protected abstract val differ:AsyncListDiffer<Song>

    var songs:List<Song>
        get() = differ.currentList
        set(value)=differ.submitList(value)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewholder {
        return SongViewholder(
            LayoutInflater.from(parent.context).inflate(layoutid,parent,false)
        )
    }



    override fun getItemCount(): Int {
        return songs.size
    }

    protected var onItemClickListener:((Song)->Unit)?=null
    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }
}