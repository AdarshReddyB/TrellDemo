package com.testing.trelldemo

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.cleveroad.bootstrap.kotlin_ffmpeg_video_compress.CompressVideoBuilder
import com.testing.trelldemo.databinding.ActivityPlayVideoBinding
import kotlinx.android.synthetic.main.activity_play_video.*
import java.io.*


class PlayVideoActivity : AppCompatActivity() {

    private lateinit var activityPlayVideoBinding: ActivityPlayVideoBinding
    private val EXTERNAL_STORAGE_PERMISSION = 101
    var path: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPlayVideoBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_play_video)

        if (intent.extras == null) {
            finish()
        }
        path = intent.extras?.getString(Constants.SELECTED_VIDEO_PATH)!!
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            initializePlayer()
        }
        activityPlayVideoBinding.buttonCompress.setOnClickListener {
            compressVideo()
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val READ_PERMISSION =
            ContextCompat.checkSelfPermission(this, permission)
        if (READ_PERMISSION != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                EXTERNAL_STORAGE_PERMISSION
            )
            return false
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission
            )
        ) {
            //Show Information about why you need the permission
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Need Storage Permission")
            builder.setMessage("This app needs storage permission.")
            builder.setPositiveButton("Grant") { dialog, which ->
                dialog.cancel()
                ActivityCompat.requestPermissions(
                    this@PlayVideoActivity,
                    arrayOf<String>(permission),
                    EXTERNAL_STORAGE_PERMISSION
                )
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            builder.show()
            return true
        }
        return true
    }

    private fun initializePlayer() {
        val mediaController = MediaController(this)
        mediaController.setMediaPlayer(videoView)
        activityPlayVideoBinding.videoView.setMediaController(mediaController)
        activityPlayVideoBinding.videoView.setVideoPath(path)
        activityPlayVideoBinding.videoView.requestFocus()
        activityPlayVideoBinding.videoView.start()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(
                            applicationContext,
                            "Please allow storage permission",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        initializePlayer()
                    }
                }
            }
        }
    }

    private fun compressVideo() {

        val split = path.split("/")
        val relativeFileName = split[split.lastIndex]

        val directory: File
        var fileName: File? = null

        try {
            directory =
                this.getDir("CompressedVideos", Context.MODE_PRIVATE) //Creating an internal dir;
            fileName =
                File(directory, "compressed_$relativeFileName") //Getting a file within the dir.
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (fileName == null) {
            return
        }

        activityPlayVideoBinding.progressBar.visibility = View.VISIBLE
        //Compress video using library
        CompressVideoBuilder.with(this)
            .setInput(path)
            .setOutputPath(fileName?.absolutePath.toString())
            .setApproximateVideoSizeMb(10)
            //.setCustomBitrate(128000, 450000)
            .execute({ result -> onResultCompressVideo(result) },
                { error -> onError(error) },
                { progress -> onProgress(progress) })
    }

    private fun onResultCompressVideo(pathname: String) {

        activityPlayVideoBinding.progressBar.visibility = View.GONE

        val inputSize = File(path).length()
        val outputSize = File(pathname).length()
        Log.d(
            "output",
            "Picked file size - $inputSize/1024 \n Output file size - $outputSize/1024 \n"
        )

        val intent = Intent(
            this,
            PlayCompressedVideoActivity::class.java
        )
        intent.putExtra(Constants.FILE_NAME, pathname)
        startActivity(intent)
    }

    private fun onError(error: Throwable) {
        activityPlayVideoBinding.progressBar.visibility = View.GONE
    }

    private fun onProgress(progress: Long) {
        //TODO
    }

    override fun onBackPressed() {
        super.onBackPressed()
        releasePlayer()
    }

    private fun releasePlayer() {
        activityPlayVideoBinding.videoView.stopPlayback()
    }

}