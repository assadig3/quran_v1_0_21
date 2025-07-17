import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hag.al_quran.R

class FavoritePagesAdapter(
    private val pages: List<Int>,
    private val onClick: (Int) -> Unit,
    private val onDelete: ((Int) -> Unit)? = null // لجعل حذف المفضلة اختياري
) : RecyclerView.Adapter<FavoritePagesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.bookmarkTitle)
        val hint = view.findViewById<TextView>(R.id.bookmarkHint)
        val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = pages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val page = pages[position]
        holder.title.text = "⭐ الصفحة $page"
        holder.hint.text = "اضغط للانتقال إلى الصفحة"
        holder.itemView.setOnClickListener { onClick(page) }
        holder.deleteIcon.setOnClickListener { onDelete?.invoke(page) }
        holder.deleteIcon.visibility = View.VISIBLE // دائماً تظهر للمفضلة
    }
}
