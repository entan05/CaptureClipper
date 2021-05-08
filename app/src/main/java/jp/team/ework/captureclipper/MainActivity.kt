package jp.team.ework.captureclipper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import jp.team.ework.captureclipper.databinding.ActivityMainBinding
import jp.team.eworks.e_core_library.activity.IndicatorActivity
import jp.team.eworks.e_core_library.view.IndicatorView
import java.io.BufferedInputStream
import java.io.FileNotFoundException

class MainActivity: IndicatorActivity<IndicatorView>() {
    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 500
        private const val REQUEST_GALLERY = 1000

        private val STORAGE_PERMISSION = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private val bind by activityBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                STORAGE_PERMISSION.toTypedArray(), REQUEST_STORAGE_PERMISSION
            )
            return
        }

        initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_STORAGE_PERMISSION -> initView()

                REQUEST_GALLERY -> {
                    data?.data?.let { uri ->
                        openImage(uri)
                    }
                }
            }
        }
    }

    override fun createIndicatorView(): IndicatorView = IndicatorView(this)

    private fun initView() {
        bind.choseImageButton.setOnClickListener {
            startUpGallery()
        }
    }

    private fun startUpGallery() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }, REQUEST_GALLERY)
    }

    private fun openImage(uri: Uri) {
        try {
            val inputStream = BufferedInputStream(contentResolver.openInputStream(uri))
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bind.imageView.setImageBitmap(bitmap)
        } catch (e: FileNotFoundException) {

        }
    }
}
