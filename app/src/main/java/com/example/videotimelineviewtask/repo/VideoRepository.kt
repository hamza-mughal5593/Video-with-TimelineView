package com.example.videotimelineviewtask.repo

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.videotimelineviewtask.R
import com.example.videotimelineviewtask.repo.data.VideoData
import com.example.videotimelineviewtask.repo.data.VideoFrame

class VideoRepository (private val context: Context) {
    private val videoDataMutableLiveData = MutableLiveData<VideoData>()

    fun getVideoData(): LiveData<VideoData> {


        val videoUri: Uri = Uri.parse("android.resource://${context.packageName}/${R.raw.sample}")

        val frames = (0..10).map {
            VideoFrame(it.toLong(), Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))
        }
        val videoData = VideoData(videoUri, frames)
        videoDataMutableLiveData.postValue(videoData)
        return videoDataMutableLiveData


    }
}