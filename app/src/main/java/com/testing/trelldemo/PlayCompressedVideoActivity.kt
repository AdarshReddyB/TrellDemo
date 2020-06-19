package com.testing.trelldemo

import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.testing.trelldemo.databinding.ActivityPlayCompressedVideoBinding

class PlayCompressedVideoActivity : AppCompatActivity() {

    private lateinit var activityPlayCompressedVideoBinding: ActivityPlayCompressedVideoBinding
    var fileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPlayCompressedVideoBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_play_compressed_video)
        if (intent.extras == null) {
            finish()
        }
        fileName = intent.extras?.getString(Constants.FILE_NAME)!!
        initializePlayer()
    }

    private fun initializePlayer() {
        val mediaController = MediaController(this)
        mediaController.setMediaPlayer(activityPlayCompressedVideoBinding.videoView)
        activityPlayCompressedVideoBinding.videoView.setMediaController(mediaController)
        activityPlayCompressedVideoBinding.videoView.setVideoPath(fileName)
        activityPlayCompressedVideoBinding.videoView.requestFocus()
        activityPlayCompressedVideoBinding.videoView.start()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        releasePlayer()
    }

    private fun releasePlayer() {
        if (activityPlayCompressedVideoBinding.videoView.isPlaying!!) {
            activityPlayCompressedVideoBinding.videoView.stopPlayback()
        }
    }
}