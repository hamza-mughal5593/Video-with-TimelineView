package com.example.videotimelineviewtask.repo.data

import android.graphics.Bitmap
import android.net.Uri

data class VideoData(
    val uri: Uri,
    val frames: List<VideoFrame>
)

data class VideoFrame(
    val timeStamp: Long,
    val bitmap: Bitmap
)
