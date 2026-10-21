package bd.edu.diu.lostandfound.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bd.edu.diu.lostandfound.R
import bd.edu.diu.lostandfound.models.Item
import com.bumptech.glide.Glide

class ItemAdapter(
    private var itemList: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivItem: ImageView = itemView.findViewById(R.id.ivItemImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvItemCategory)
        val tvLocation: TextView = itemView.findViewById(R.id.tvItemLocation)
        val tvDate: TextView = itemView.findViewById(R.id.tvItemDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvItemStatus)
        val tvTypeBadge: TextView = itemView.findViewById(R.id.tvItemTypeBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        val context = holder.itemView.context

        holder.tvTitle.text = item.title
        holder.tvCategory.text = item.category.ifEmpty { "General" }
        holder.tvLocation.text = item.location.ifEmpty { "Campus" }
        holder.tvDate.text = item.date.ifEmpty { "N/A" }
        holder.tvStatus.text = "• ${item.status.ifEmpty { "Active" }}"

        // Configure Type Badge (Lost vs Found)
        if (item.type.equals("Found", ignoreCase = true)) {
            holder.tvTypeBadge.text = "Found"
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_found)
            holder.tvTypeBadge.setTextColor(Color.parseColor("#16A34A"))
        } else {
            holder.tvTypeBadge.text = "Lost"
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_lost)
            holder.tvTypeBadge.setTextColor(Color.parseColor("#DC2626"))
        }

        // Load Thumbnail Image
        if (item.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.ivItem)
        } else {
            holder.ivItem.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = itemList.size

    fun updateList(newList: List<Item>) {
        itemList = newList
        notifyDataSetChanged()
    }
}
