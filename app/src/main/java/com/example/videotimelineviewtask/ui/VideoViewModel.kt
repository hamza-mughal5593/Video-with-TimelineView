package com.example.videotimelineviewtask.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.videotimelineviewtask.repo.data.VideoData
import com.example.videotimelineviewtask.repo.VideoRepository

class VideoViewModel (application: Application) : AndroidViewModel(application) {
    private val videoRepository: VideoRepository = VideoRepository(application)
    val videoData: LiveData<VideoData> = videoRepository.getVideoData()
}