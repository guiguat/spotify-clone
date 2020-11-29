package com.example.mvvmspotifyclone.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.RequestManager
import com.example.mvvmspotifyclone.R.layout.activity_main
import com.example.mvvmspotifyclone.adapters.SwipeSongAdapter
import com.example.mvvmspotifyclone.data.entities.Song
import com.example.mvvmspotifyclone.data.util.Status
import com.example.mvvmspotifyclone.data.util.Status.*
import com.example.mvvmspotifyclone.exoplayer.toSong
import com.example.mvvmspotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter
    @Inject
    lateinit var glide: RequestManager
    private var curPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        subscribeToObservers()
        vpSong.adapter = swipeSongAdapter
    }

    private fun switchViewPagerToCurrentSong(song: Song){
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex != -1){
            vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(this){
            it?.let { res ->
                when(res.status){
                    SUCCESS -> {
                        res.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if(songs.isNotEmpty()){
                                glide.load((curPlayingSong?: songs[0]).imageUrl).into(ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    ERROR -> Unit
                    LOADING -> Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(this){
            if(it == null) return@observe
            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }
    }
}