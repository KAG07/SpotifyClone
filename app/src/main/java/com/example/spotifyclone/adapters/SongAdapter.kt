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
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide:RequestManager
) :RecyclerView.Adapter<SongAdapter.SongViewholder>(){

class SongViewholder(itemview:View):RecyclerView.ViewHolder(itemview)


    private val diffcallback=object :DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId==newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }
    }

    private val differ=AsyncListDiffer(this,diffcallback)

    var songs:List<Song>
    get() = differ.currentList
    set(value)=differ.submitList(value)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewholder {
        return SongViewholder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        )
    }

    override fun onBindViewHolder(holder: SongViewholder, position: Int) {
        val song=songs[position]
        holder.itemView.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.Subtitle
            glide.load(song.imageurl).into(ivItemImage)
            setOnClickListener {
                onItemClickListener?.let {
                    it(song)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    private var onItemClickListener:((Song)->Unit)?=null
    fun setOnItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }
}