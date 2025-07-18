import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hag.al_quran.R
import com.hag.al_quran.addFavoriteAyah
import com.hag.al_quran.isFavoriteAyah
import com.hag.al_quran.removeFavoriteAyah

class AyahAdapter(
    private val surahNumber: Int,
    private val ayahList: List<Pair<Int, String>>, // كل عنصر: رقم الآية، نص الآية
    private val onAyahClick: (Int, String) -> Unit
) : RecyclerView.Adapter<AyahAdapter.AyahViewHolder>() {

    inner class AyahViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ayahText: TextView = view.findViewById(R.id.ayahText)
        val favIcon: ImageView = view.findViewById(R.id.favIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyahViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ayah, parent, false)
        return AyahViewHolder(v)
    }

    override fun onBindViewHolder(holder: AyahViewHolder, position: Int) {
        val (ayahNumber, ayahText) = ayahList[position]
        holder.ayahText.text = ayahText

        // شكل النجمة حسب الحفظ
        if (isFavoriteAyah(holder.itemView.context, surahNumber, ayahNumber)) {
            holder.favIcon.setImageResource(R.drawable.ic_star_filled)
        } else {
            holder.favIcon.setImageResource(R.drawable.ic_star_border)
        }

        // الحفظ أو الإزالة عند الضغط
        holder.favIcon.setOnClickListener {
            val ctx = holder.itemView.context
            if (isFavoriteAyah(ctx, surahNumber, ayahNumber)) {
                removeFavoriteAyah(ctx, surahNumber, ayahNumber)
                holder.favIcon.setImageResource(R.drawable.ic_star_border)
            } else {
                addFavoriteAyah(ctx, surahNumber, ayahNumber)
                holder.favIcon.setImageResource(R.drawable.ic_star_filled)
            }
        }

        // عند الضغط على العنصر (لأي وظيفة تريدها)
        holder.itemView.setOnClickListener {
            onAyahClick(ayahNumber, ayahText)
        }
    }

    override fun getItemCount() = ayahList.size
}
