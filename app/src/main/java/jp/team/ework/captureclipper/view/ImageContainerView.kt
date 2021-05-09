package jp.team.ework.captureclipper.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import jp.team.ework.captureclipper.R

class ImageContainerView: ConstraintLayout {

    private var bitmap: Bitmap? = null

    private val imageView: ImageView
    private val emptyLabel: TextView

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(
        context,
        attrs,
        defStyleAttr
    ) {
        View.inflate(context, R.layout.view_image_container, this)

        imageView = findViewById(R.id.image_view)
        emptyLabel = findViewById(R.id.empty_label)
    }

    fun setEmptyLabel(text: String?) {
        emptyLabel.text = text
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        imageView.apply {
            setImageBitmap(bitmap)
            visibility = View.VISIBLE
        }
        emptyLabel.visibility = View.GONE
    }

    fun getBitmap(): Bitmap? = bitmap

    fun clear() {
        bitmap = null
        imageView.visibility = View.INVISIBLE
        emptyLabel.visibility = View.VISIBLE
    }
}
