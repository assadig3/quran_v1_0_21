package com.hag.al_quran

import Juz
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class JuzListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JuzAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // ⭐ لإظهار القائمة العلوية (Menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_juz_list, container, false)
        recyclerView = view.findViewById(R.id.juzRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val juzList = listOf(
            Juz(1, "الجزء الأول", 1),
            Juz(2, "الجزء الثاني", 22),
            Juz(3, "الجزء الثالث", 42),
            Juz(4, "الجزء الرابع", 62),
            Juz(5, "الجزء الخامس", 82),
            Juz(6, "الجزء السادس", 102),
            Juz(7, "الجزء السابع", 121),
            Juz(8, "الجزء الثامن", 142),
            Juz(9, "الجزء التاسع", 162),
            Juz(10, "الجزء العاشر", 182),
            Juz(11, "الجزء الحادي عشر", 201),
            Juz(12, "الجزء الثاني عشر", 222),
            Juz(13, "الجزء الثالث عشر", 242),
            Juz(14, "الجزء الرابع عشر", 262),
            Juz(15, "الجزء الخامس عشر", 282),
            Juz(16, "الجزء السادس عشر", 302),
            Juz(17, "الجزء السابع عشر", 322),
            Juz(18, "الجزء الثامن عشر", 342),
            Juz(19, "الجزء التاسع عشر", 362),
            Juz(20, "الجزء العشرون", 382),
            Juz(21, "الجزء الحادي والعشرون", 402),
            Juz(22, "الجزء الثاني والعشرون", 422),
            Juz(23, "الجزء الثالث والعشرون", 442),
            Juz(24, "الجزء الرابع والعشرون", 462),
            Juz(25, "الجزء الخامس والعشرون", 482),
            Juz(26, "الجزء السادس والعشرون", 502),
            Juz(27, "الجزء السابع والعشرون", 522),
            Juz(28, "الجزء الثامن والعشرون", 542),
            Juz(29, "الجزء التاسع والعشرون", 562),
            Juz(30, "الجزء الثلاثون", 582)
        )
        adapter = JuzAdapter(juzList) { juz ->
            // حفظ آخر جزء تم قراءته
            saveLastReadJuz(requireContext(), juz.pageNumber)
            val intent = Intent(requireContext(), QuranPageActivity::class.java)
            intent.putExtra("page_number", juz.pageNumber)
            intent.putExtra("juz_name", juz.name)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_juz, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_last_read -> {
                // فتح آخر جزء تم قراءته (من الحفظ)
                val prefs = requireContext().getSharedPreferences("juz_prefs", Context.MODE_PRIVATE)
                val lastJuzPage = prefs.getInt("last_juz_page", 1)
                val intent = Intent(requireContext(), QuranPageActivity::class.java)
                intent.putExtra("page_number", lastJuzPage)
                startActivity(intent)
                return true
            }
            R.id.action_jump_to_juz -> {
                // كود البحث (يمكنك إظهار Dialog بحث هنا)
                Toast.makeText(requireContext(), "ميزة البحث قيد التطوير!", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.action_jump -> {
                // كود الانتقال لسورة (يمكنك إظهار Dialog لاختيار السورة)
                Toast.makeText(requireContext(), "انتقال إلى سورة قيد التطوير!", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // دالة حفظ آخر جزء تم قراءته
    private fun saveLastReadJuz(context: Context, pageNumber: Int) {
        val prefs = context.getSharedPreferences("juz_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("last_juz_page", pageNumber).apply()
    }
}
