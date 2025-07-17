package com.hag.al_quran

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class SurahAdapter(
    private val surahList: List<Surah>,
    private val onItemClick: (Surah) -> Unit
) : RecyclerView.Adapter<SurahAdapter.SurahViewHolder>() {

    inner class SurahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val surahNumber: TextView = itemView.findViewById(R.id.surahNumber)
        val surahTitle: TextView = itemView.findViewById(R.id.surahTitle)
        val favIcon: ImageView = itemView.findViewById(R.id.favIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_surah, parent, false)
        return SurahViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        val surah = surahList[position]
        val context = holder.itemView.context

        // رقم السورة بالأرقام العربية
        holder.surahNumber.text = convertToArabicNumber(surah.number)

        // عنوان السورة: اسم - نوع - عدد الآيات
        holder.surahTitle.text = "${surah.name} (${surah.type} • ${surah.ayahCount} آيات)"

        // تلوين الدائرة حسب نوع السورة (اختياري - أضف ملفيّ bg_surah_makki/bg_surah_madani إذا أردت)
        /*
        if (surah.type == "مكية") {
            holder.surahNumber.setBackgroundResource(R.drawable.bg_surah_makki)
        } else {
            holder.surahNumber.setBackgroundResource(R.drawable.bg_surah_madani)
        }
        */

        // أيقونة النجمة (مفضلة أو لا)
        if (isFavorite(context, surah.number)) {
            holder.favIcon.setImageResource(R.drawable.ic_star_filled)
        } else {
            holder.favIcon.setImageResource(R.drawable.ic_star_outline)
        }

        holder.favIcon.setOnClickListener {
            if (isFavorite(context, surah.number)) {
                removeFavorite(context, surah.number)
                holder.favIcon.setImageResource(R.drawable.ic_star_outline)
                Toast.makeText(context, "تمت إزالة السورة من المفضلة", Toast.LENGTH_SHORT).show()
            } else {
                addFavorite(context, surah.number)
                holder.favIcon.setImageResource(R.drawable.ic_star_filled)
                Toast.makeText(context, "تمت إضافة السورة للمفضلة", Toast.LENGTH_SHORT).show()
            }
            // لتنبيه RecyclerView لو أردت: notifyItemChanged(position)
        }

        holder.itemView.setOnClickListener { onItemClick(surah) }
    }

    override fun getItemCount(): Int = surahList.size

    // تحويل الأرقام إلى العربية
    private fun convertToArabicNumber(number: Int): String {
        val arabicNums = arrayOf('٠','١','٢','٣','٤','٥','٦','٧','٨','٩')
        return number.toString().map { arabicNums[it.digitToInt()] }.joinToString("")
    }

    // دوال المفضلة (يمكن نقلها إلى Utils خارجي لو رغبت)
    private fun addFavorite(context: Context, surahNumber: Int) {
        val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("fav_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(surahNumber.toString())
        prefs.edit().putStringSet("fav_list", set).apply()
    }

    private fun removeFavorite(context: Context, surahNumber: Int) {
        val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("fav_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.remove(surahNumber.toString())
        prefs.edit().putStringSet("fav_list", set).apply()
    }

    private fun isFavorite(context: Context, surahNumber: Int): Boolean {
        val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("fav_list", mutableSetOf()) ?: mutableSetOf()
        return set.contains(surahNumber.toString())
    }
}
