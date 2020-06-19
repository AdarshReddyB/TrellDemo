package com.testing.trelldemo

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.testing.trelldemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private val SELECT_VIDEO: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityMainBinding.btnPickVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_VIDEO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_VIDEO) {
                val selectedImageUri: Uri? = data?.data

                // MEDIA GALLERY
                var selectedVideoPath = getPath(selectedImageUri)
                if (selectedVideoPath != null) {
                    val intent = Intent(
                        this,
                        PlayVideoActivity::class.java
                    )
                    intent.putExtra(Constants.SELECTED_VIDEO_PATH, selectedVideoPath)
                    startActivity(intent)
                }
            }
        }
    }

    private fun getPath(uri: Uri?): String? {
        val projection =
            arrayOf<String>(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }

}
