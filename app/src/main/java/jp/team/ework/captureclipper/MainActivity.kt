package jp.team.ework.captureclipper

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import jp.team.ework.captureclipper.databinding.ActivityMainBinding
import jp.team.ework.captureclipper.extension.*
import jp.team.eworks.e_core_library.activity.IndicatorActivity
import jp.team.eworks.e_core_library.view.IndicatorView
import java.io.BufferedInputStream
import java.io.FileNotFoundException
import java.util.*
import kotlin.math.abs

class MainActivity: IndicatorActivity<IndicatorView>() {
    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 500
        private const val REQUEST_GALLERY = 1000

        private val STORAGE_PERMISSION = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        private const val IMAGE_MIME_TYPE = "image/png"
    }

    private val bind by activityBinding<ActivityMainBinding>()

    private val mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val otherHandler: Handler by lazy {
        val handlerThread = HandlerThread("other thread")
        handlerThread.start()
        Handler(handlerThread.looper)
    }

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
                REQUEST_GALLERY -> {
                    data?.data?.let { uri ->
                        openImage(uri)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initView()
                } else {
                    finish()
                }
            }
        }
    }

    override fun createIndicatorView(): IndicatorView = IndicatorView(this)

    private fun initView() {
        bind.selectImage.apply {
            setEmptyLabel("画像を選択")
            setOnClickListener {
                startUpGallery()
            }
        }

        bind.clipImage.setEmptyLabel("加工後画像はまだありません")

        bind.saveClipImageButton.setOnClickListener {
            saveImage()
        }
    }

    private fun startUpGallery() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = IMAGE_MIME_TYPE
        }, REQUEST_GALLERY)
    }

    private fun openImage(uri: Uri) {
        try {
            val inputStream = BufferedInputStream(contentResolver.openInputStream(uri))
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bind.selectImage.setBitmap(bitmap)

            indicatorView.message = "画像処理中"
            showIndicator()
            otherHandler.post {
                val clipImage = clipImage(bitmap)
                mainHandler.post {
                    bind.clipImage.setBitmap(clipImage)
                    bind.saveClipImageButton.isEnabled = true
                    hideIndicator()
                }
            }
        } catch (e: FileNotFoundException) {

        }
    }

    private fun clipImage(bitmap: Bitmap): Bitmap {
        val cBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val originWidth = cBitmap.width
        val originHeight = cBitmap.height
        // pxの取得
        val pixels = IntArray(originWidth * originHeight)
        cBitmap.getPixels(pixels, 0, originWidth, 0, 0, originWidth, originHeight)

        var startX = originWidth
        var startY = originHeight
        var endX = 0
        var endY = 0
        for (y in 0 until originHeight) {
            for (x in 0 until originWidth) {
                val px = pixels[x + (y * originWidth)]

                if (px != Color.BLACK) {
                    if (x < startX) startX = x
                    if (y < startY) startY = y
                    if (x > endX) endX = x
                    if (y > endY) endY = y
                }
            }
        }

        return Bitmap.createBitmap(
            bitmap,
            startX,
            startY,
            abs(endX - startX),
            abs(endY - startY),
            null,
            true
        )
    }

    private fun saveImage() {
        indicatorView.message = "保存処理中"
        showIndicator()

        val outputBitmap = bind.clipImage.getBitmap()

        otherHandler.post {
            val values = ContentValues().apply {
                // ファイル名
                put(MediaStore.Images.Media.DISPLAY_NAME, getSaveImageName())
                // mime type
                put(MediaStore.Images.Media.MIME_TYPE, IMAGE_MIME_TYPE)
                // 排他アクセス
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val contentUri =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val resolver = applicationContext.contentResolver
            val insertItem = resolver.insert(contentUri, values)

            if (insertItem != null && outputBitmap != null) {
                try {
                    val outputStream = contentResolver.openOutputStream(insertItem)
                    outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                } catch (e: FileNotFoundException) {

                }
                values.apply {
                    clear()
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(insertItem, values, null, null)
            }

            mainHandler.post {
                bind.selectImage.clear()
                bind.clipImage.clear()
                bind.saveClipImageButton.isEnabled = false
                hideIndicator()
            }
        }
    }

    private fun getSaveImageName(): String {
        val calendar = Calendar.getInstance()
        return String.format(
            "clipImage_%04d%02d%02d-%02d%02d%02d.png",
            calendar.year, calendar.month + 1, calendar.date,
            calendar.hourOfDay, calendar.minute, calendar.second
        )
    }
}
