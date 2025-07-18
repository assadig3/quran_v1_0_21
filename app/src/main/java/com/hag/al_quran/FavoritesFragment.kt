package com.hag.al_quran

import FavoritePagesAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritePagesAdapter
    private lateinit var recentListLayout: LinearLayout
    private lateinit var titleRecent: TextView
    private lateinit var titleFavorites: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recentListLayout = view.findViewById(R.id.recentPagesList)
        titleRecent = view.findViewById(R.id.titleRecent)
        titleFavorites = view.findViewById(R.id.titleFavorites)
        return view
    }

    override fun onResume() {
        super.onResume()
        showRecentPages()
        loadFavoritePages()
    }

    private fun loadFavoritePages() {
        val pages = getAllFavoritePages(requireContext()).toMutableList()
        adapter = FavoritePagesAdapter(pages, { page ->
            val intent = Intent(requireContext(), QuranPageActivity::class.java)
            intent.putExtra("page", page)
            startActivity(intent)
        })
        recyclerView.adapter = adapter

        // إظهار/إخفاء عنوان المفضلات بناءً على وجود عناصر
        titleFavorites.visibility = if (pages.isEmpty()) View.GONE else View.VISIBLE

        if (pages.isEmpty()) {
            Toast.makeText(requireContext(), "لا توجد صفحات مفضّلة", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRecentPages() {
        recentListLayout.removeAllViews()
        val lastPages = getRecentPages(requireContext())
        val inflater = LayoutInflater.from(requireContext())

        if (lastPages.isNullOrEmpty()) {
            titleRecent.visibility = View.GONE
        } else {
            titleRecent.visibility = View.VISIBLE
            for (page in lastPages) {
                val card = inflater.inflate(R.layout.item_bookmark, recentListLayout, false)
                val title = card.findViewById<TextView>(R.id.bookmarkTitle)
                val hint = card.findViewById<TextView>(R.id.bookmarkHint)
                val deleteIcon = card.findViewById<ImageView>(R.id.deleteIcon)

                title.text = "🕓 الصفحة $page"
                hint.text = "اضغط للانتقال إلى الصفحة"
                card.setOnClickListener {
                    val intent = Intent(requireContext(), QuranPageActivity::class.java)
                    intent.putExtra("page", page)
                    startActivity(intent)
                }
                deleteIcon.visibility = View.GONE // لا تظهر أيقونة الحذف في "آخر المتصفحات"

                recentListLayout.addView(card)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_favorites, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all_bookmarks -> {
                clearAllBookmarks()
                true
            }
            R.id.action_clear_all_recent -> {
                clearAllRecent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearAllBookmarks() {
        val prefs = requireContext().getSharedPreferences("favorite_pages", Context.MODE_PRIVATE)
        prefs.edit().remove("pages").apply()
        loadFavoritePages()
        Toast.makeText(requireContext(), "تم حذف كل الصفحات المحفوظة", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllRecent() {
        val prefs = requireContext().getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("recent_pages_str").apply()
        showRecentPages()
        Toast.makeText(requireContext(), "تم حذف سجل التصفح", Toast.LENGTH_SHORT).show()
    }
}
