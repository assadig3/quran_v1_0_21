package com.hag.al_quran

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.hag.al_quran.audio.AudioPlayer
import com.hag.al_quran.audio.MadaniPageProvider
import org.json.JSONArray
import org.json.JSONObject

class AssetPageAdapter(
    private val context: Context,
    private val pages: List<String>,
    private val realPageNumber: Int,
    private val onAyahClick: (surah: Int, ayah: Int) -> Unit,
    private val onImageTap: () -> Unit
) : RecyclerView.Adapter<AssetPageAdapter.PageViewHolder>() {

    var selectedAyah: Pair<Int, Int>? = null
    private val madaniProvider = MadaniPageProvider(context)
    private val currentQari = madaniProvider.getQariById("muaiqly")

    class PageViewHolder(
        val root: FrameLayout,
        val photoView: PhotoView,
        val ayahOverlay: FrameLayout
    ) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val root = FrameLayout(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val photoView = PhotoView(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_XY // ليملأ الصورة تماماً
            maximumScale = 3f
            minimumScale = 1f
            mediumScale = 2f
        }
        root.addView(photoView)

        val ayahOverlay = FrameLayout(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
        }
        root.addView(ayahOverlay)

        photoView.setOnClickListener { onImageTap() }

        return PageViewHolder(root, photoView, ayahOverlay)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val pageName = pages[position]
        Log.d("AssetPageAdapter", "Loading asset file: pages/$pageName")

        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inDither = true
        }

        val inputStream = context.assets.open("pages/$pageName")
        val bitmap = BitmapFactory.decodeStream(inputStream, null, options)

        // قص الهوامش البيضاء تلقائيًا
        val croppedBitmap = bitmap?.let { cropWhiteMargins(it) }
        holder.photoView.setImageBitmap(croppedBitmap)

        holder.photoView.setScale(1f, true)
        holder.photoView.setTranslationX(0f)
        holder.photoView.setTranslationY(0f)
        holder.photoView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        if (isNight) {
            holder.photoView.setColorFilter(Color.argb(100, 0, 0, 0), android.graphics.PorterDuff.Mode.DARKEN)
        } else {
            holder.photoView.clearColorFilter()
        }
    }

    override fun getItemCount(): Int = pages.size

    // ====== دالة قص الهوامش البيضاء =======
    private fun cropWhiteMargins(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        var top = 0
        var left = 0
        var right = width - 1
        var bottom = height - 1
        var found = false

        // Crop from top
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (source.getPixel(x, y) != Color.WHITE && source.getPixel(x, y) != Color.TRANSPARENT) {
                    top = y
                    found = true
                    break
                }
            }
            if (found) break
        }
        // Crop from bottom
        found = false
        for (y in height - 1 downTo 0) {
            for (x in 0 until width) {
                if (source.getPixel(x, y) != Color.WHITE && source.getPixel(x, y) != Color.TRANSPARENT) {
                    bottom = y
                    found = true
                    break
                }
            }
            if (found) break
        }
        // Crop from left
        found = false
        for (x in 0 until width) {
            for (y in top..bottom) {
                if (source.getPixel(x, y) != Color.WHITE && source.getPixel(x, y) != Color.TRANSPARENT) {
                    left = x
                    found = true
                    break
                }
            }
            if (found) break
        }
        // Crop from right
        found = false
        for (x in width - 1 downTo 0) {
            for (y in top..bottom) {
                if (source.getPixel(x, y) != Color.WHITE && source.getPixel(x, y) != Color.TRANSPARENT) {
                    right = x
                    found = true
                    break
                }
            }
            if (found) break
        }

        // إذا لم يتم العثور على حواف غير بيضاء، أرجع الصورة الأصلية
        if (right <= left || bottom <= top) return source

        return Bitmap.createBitmap(source, left, top, right - left + 1, bottom - top + 1)
    }
}
