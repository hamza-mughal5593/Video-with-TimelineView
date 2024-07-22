package com.example.videotimelineviewtask.ui

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.videotimelineviewtask.R
import com.example.videotimelineviewtask.timelineView.BarThumb
import com.example.videotimelineviewtask.timelineView.CustomRangeSeekBar
import com.example.videotimelineviewtask.timelineView.OnRangeSeekBarChangeListener
import com.example.videotimelineviewtask.databinding.FragmentVideoEditBinding
import java.util.Locale
import kotlin.math.max
import kotlin.math.min


class VideoEditFragment : Fragment() {

    private lateinit var binding: FragmentVideoEditBinding

    private lateinit var videoViewModel: VideoViewModel




    private var mTimeVideo = 0
    private var mDuration = 0
    private var mStartPosition = 0
    private var mEndPosition = 0

    // set your max video trim seconds
    private val mMaxDuration = 60
    private val mHandler: Handler = Handler()



    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector
    private var scaleFactor = 1.0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f
    private var isScaling = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentVideoEditBinding.inflate(layoutInflater, container, false)


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoViewModel = ViewModelProvider(this).get(VideoViewModel::class.java)

        videoViewModel.videoData.observe(viewLifecycleOwner) { videoData ->

            setupTimeLineView(videoData.uri)
        }
    }

    private fun setupTimeLineView(uri: Uri) {


        binding.videoView.setOnPreparedListener(this::onVideoPrepared)

        binding.videoView.setOnCompletionListener { mp -> onVideoCompleted() }


        binding.timeLineView.post {
            if (uri != null) {
                setBitmap(uri)
                binding.videoView.setVideoURI(uri)
            }
        }

        binding.timeLineBar.addOnRangeSeekBarListener(object :
            OnRangeSeekBarChangeListener {
            override fun onCreate(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                // Do nothing
            }

            override fun onSeek(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                mHandler.removeCallbacks(mUpdateTimeTask)
                binding.videoView.seekTo(mStartPosition * 1000)
                binding.videoView.pause()
                binding.imgPlay.setBackgroundResource(R.drawable.play)
            }

            override fun onSeekStop(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
            }
        })

        binding.imgPlay.setOnClickListener {
            if (binding.videoView.isPlaying()) {
                binding.videoView.pause()
                binding.imgPlay.setBackgroundResource(R.drawable.play)
            } else {
                binding.videoView.start();
                binding.imgPlay.setBackgroundResource(R.drawable.pause)

            }
        }

        scaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleListener())
        gestureDetector = GestureDetector(requireContext(), GestureListener())



        binding.videoFrame.setOnTouchListener { _, event ->
            // Handle scaling first
            scaleGestureDetector.onTouchEvent(event)

            // Handle panning only if scaling is not in progress
            if (!isScaling) {
                gestureDetector.onTouchEvent(event)

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastTouchX = event.x
                        lastTouchY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.x - lastTouchX
                        val dy = event.y - lastTouchY
                        posX += dx
                        posY += dy
                        lastTouchX = event.x
                        lastTouchY = event.y
                        applyTransformations()
                    }
                }
            }

            // Update isScaling state based on scale gesture events
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                isScaling = false
            }

            true
        }


    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            return true
        }

        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            scaleFactor *= scaleGestureDetector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f)
            applyTransformations()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (!isScaling) {
                posX -= distanceX
                posY -= distanceY
                applyTransformations()
            }
            return true
        }
    }

    private fun applyTransformations() {
        val viewWidth = binding.videoFrame.width.toFloat()
        val viewHeight = binding.videoFrame.height.toFloat()
        val contentWidth = viewWidth * scaleFactor
        val contentHeight = viewHeight * scaleFactor

        posX = max(min(posX, (contentWidth - viewWidth) / 2), -(contentWidth - viewWidth) / 2)
        posY = max(min(posY, (contentHeight - viewHeight) / 2), -(contentHeight - viewHeight) / 2)

        binding.videoFrame.pivotX = viewWidth / 2
        binding.videoFrame.pivotY = viewHeight / 2
        binding.videoFrame.scaleX = scaleFactor
        binding.videoFrame.scaleY = scaleFactor
        binding.videoFrame.translationX = posX
        binding.videoFrame.translationY = posY
    }





    private fun setBitmap(mVideoUri: Uri) {
        binding.timeLineView.setVideo(mVideoUri, requireActivity())
    }

    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            BarThumb.LEFT -> {
                mStartPosition = ((mDuration * value / 100L).toInt())
                binding.videoView.seekTo(mStartPosition * 1000)
            }

            BarThumb.RIGHT -> {
                mEndPosition = ((mDuration * value / 100L).toInt())
            }
        }
        mTimeVideo = mEndPosition - mStartPosition

        binding.videoView.seekTo(mStartPosition * 1000)
        var mStart: String =  "$mStartPosition"
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        val startMin = mStart.toInt() / 60
        val startSec = mStart.toInt() % 60
        var mEnd: String =   "$mEndPosition"
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        val endMin = mEnd.toInt() / 60
        val endSec = mEnd.toInt() % 60
        binding.txtVideoTrimSeconds.setText(
            String.format(
                Locale.US,
                "%02d:%02d - %02d:%02d",
                startMin,
                startSec,
                endMin,
                endSec
            )
        )
    }
    private val mUpdateTimeTask: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {

                mHandler.postDelayed(this, 100)
        }
    }

    private fun onVideoPrepared(mp: MediaPlayer) {

        mDuration = binding.videoView.duration / 1000
        setSeekBarPosition()


        binding.videoView.start()

    }
    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = 0
            mEndPosition = mMaxDuration
            binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration).toFloat())
            binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration).toFloat())
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
        }
        mTimeVideo = mDuration
        binding.timeLineBar.initMaxWidth()
        binding.videoView.seekTo(mStartPosition * 1000)
        var mStart = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        val startMin = mStart.toInt() / 60
        val startSec = mStart.toInt() % 60
        var mEnd = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        val endMin = mEnd.toInt() / 60
        val endSec = mEnd.toInt() % 60
        binding.txtVideoTrimSeconds.text =
            String.format(Locale.US, "%02d:%02d - %02d:%02d", startMin, startSec, endMin, endSec)
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask)
        binding.videoView.seekTo(mStartPosition * 1000)
        binding.videoView.pause()
        binding.imgPlay.setBackgroundResource(R.drawable.play)
    }


}