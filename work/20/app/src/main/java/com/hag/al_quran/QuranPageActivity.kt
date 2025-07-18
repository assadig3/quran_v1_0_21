package com.hag.al_quran

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.hag.al_quran.audio.MadaniPageProvider
import com.hag.al_quran.audio.PageAyahMapLoader
import org.json.JSONArray

class QuranPageActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentPage = 1
    private var currentQariId: String = "fares"
    private var ayahQueue: MutableList<Triple<String, Int, Int>> = mutableListOf()

    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnQari: Button
    private lateinit var audioDownload: ImageButton
    private lateinit var audioControls: LinearLayout

    private lateinit var madaniProvider: MadaniPageProvider
    private lateinit var ayahMapLoader: PageAyahMapLoader

    private var ayahBanner: View? = null
    private var ayahTextView: TextView? = null
    private var rootLayout: ViewGroup? = null

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quran_page)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        prefs = getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)
        currentQariId = prefs.getString("qari_id", "fares") ?: "fares"

        val surahName = intent.getStringExtra("surah_name")
        val juzName = intent.getStringExtra("juz_name")
        supportActionBar?.title = surahName ?: juzName ?: "القرآن الكريم"

        audioControls = findViewById(R.id.audioControls)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnQari = findViewById(R.id.btnQari)
        audioDownload = findViewById(R.id.audio_download)

        ViewCompat.setOnApplyWindowInsetsListener(audioControls) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = bottomInset + 16)
            insets
        }

        madaniProvider = MadaniPageProvider(this)
        ayahMapLoader = PageAyahMapLoader(this)

        rootLayout = findViewById(android.R.id.content)
        val bannerView = layoutInflater.inflate(R.layout.ayah_now_playing, null)
        ayahBanner = bannerView
        ayahTextView = bannerView.findViewById(R.id.ayahText)

        // ✅ زر الإغلاق
        val btnCloseBanner = bannerView.findViewById<ImageButton>(R.id.btnCloseBanner)
        btnCloseBanner.setOnClickListener {
            hideAyahBanner()
        }

        bannerView.visibility = View.GONE
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(24, 48, 24, 0)
        bannerView.layoutParams = layoutParams
        (rootLayout as? ViewGroup)?.addView(bannerView)

        btnPlayPause.setOnClickListener {
            if (isPlaying) pauseAudio()
            else {
                prepareAudioQueueForPage(currentPage)
                playNextAyah()
            }
        }

        btnQari.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_qari_picker, null)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.qariList)
            recyclerView.layoutManager = LinearLayoutManager(this)

            val qaris = madaniProvider.getQaris()
            val qariPairs = qaris.map { it.name to it.id }

            val alertDialog = android.app.AlertDialog.Builder(this).create()
            val adapter = QariAdapter(qariPairs) { name, id ->
                currentQariId = id
                prefs.edit().putString("qari_id", id).apply()
                btnQari.text = name
                prepareAudioQueueForPage(currentPage)
                if (isPlaying) playNextAyah()
                alertDialog.dismiss()
            }

            recyclerView.adapter = adapter
            alertDialog.setView(dialogView)
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog.show()
        }

        audioDownload.setOnClickListener {
            Toast.makeText(this, "دعم التحميل قريبًا", Toast.LENGTH_SHORT).show()
        }

        val pageNumber = intent.getIntExtra("page_number", 1)
        currentPage = pageNumber

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.quran_container, QuranPageFragment.newInstance(pageNumber))
                .commit()
        }
    }

    fun onFragmentPageChanged(pageNumber: Int) {
        currentPage = pageNumber
        prepareAudioQueueForPage(pageNumber)
        if (isPlaying && ayahQueue.isNotEmpty()) {
            playNextAyah()
        }
    }

    private fun prepareAudioQueueForPage(pageNumber: Int) {
        val qari = madaniProvider.getQariById(currentQariId) ?: return
        val ayahs = ayahMapLoader.getAyahsForPage(pageNumber)
        ayahQueue.clear()
        for ((surah, ayah) in ayahs) {
            val url = madaniProvider.getAyahUrl(qari, surah, ayah)
            ayahQueue.add(Triple(url, surah, ayah))
        }
    }

    private fun playNextAyah() {
        if (ayahQueue.isEmpty()) {
            val nextPage = currentPage + 1
            if (nextPage <= 604) {
                currentPage = nextPage
                val fragment = supportFragmentManager.findFragmentById(R.id.quran_container)
                if (fragment is QuranPageFragment) {
                    fragment.goToPage(nextPage)
                }
                prepareAudioQueueForPage(currentPage)
                if (ayahQueue.isNotEmpty()) playNextAyah()
            } else {
                isPlaying = false
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            }
            return
        }

        val (url, surah, ayah) = ayahQueue.removeAt(0)
        try {
            val ayahText = madaniProvider.getAyahTextFromUrl(url, this)
            if (ayahText != null) showAyahBanner(ayahText)
            // highlightAyah(surah, ayah)   // ❌ أزل أو علق هذا السطر!

            if (mediaPlayer == null) mediaPlayer = MediaPlayer() else mediaPlayer?.reset()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.setOnPreparedListener {
                it.start()
                isPlaying = true
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
            mediaPlayer?.setOnCompletionListener {
                hideAyahBanner()
                playNextAyah()
            }
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر تشغيل التلاوة", Toast.LENGTH_SHORT).show()
            playNextAyah()
        }
    }


    private fun showAyahBanner(text: String) {
        ayahTextView?.apply {
            this.text = text
            isSelected = true  // هذا يُفعل حركة marquee
        }
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
        ayahBanner?.visibility = View.VISIBLE
        ayahBanner?.startAnimation(slideIn)
    }

    private fun hideAyahBanner() {
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_top)
        slideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                ayahBanner?.visibility = View.GONE

                // ✅ إعادة تعيين الهوامش بعد إخفاء الشريط
                val params = ayahBanner?.layoutParams as? LinearLayout.LayoutParams
                params?.setMargins(0, 0, 0, 0)
                ayahBanner?.layoutParams = params
            }
        })
        ayahBanner?.startAnimation(slideOut)
    }

    private fun highlightAyah(surah: Int, ayah: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.quran_container)
        if (fragment is QuranPageFragment) {
            fragment.highlightAyah(surah, ayah)
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
    }

    override fun onStop() {
        super.onStop()
        pauseAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

// ✅ استخراج نص الآية من URL
fun MadaniPageProvider.getAyahTextFromUrl(url: String, context: Context): String? {
    val pattern = ".*/(\\d{3})(\\d{3})\\.mp3$".toRegex()
    val match = pattern.find(url) ?: return null
    val surah = match.groupValues[1].toInt()
    val ayah = match.groupValues[2].toInt()

    val inputStream = context.assets.open("quran.json")
    val jsonStr = inputStream.bufferedReader().use { it.readText() }
    val quranArr = JSONArray(jsonStr)

    for (i in 0 until quranArr.length()) {
        val surahObj = quranArr.getJSONObject(i)
        if (surahObj.getInt("id") == surah) {
            val versesArr = surahObj.getJSONArray("verses")
            for (j in 0 until versesArr.length()) {
                val verseObj = versesArr.getJSONObject(j)
                if (verseObj.getInt("id") == ayah) {
                    return verseObj.getString("text")
                }
            }
        }
    }
    return null
}
