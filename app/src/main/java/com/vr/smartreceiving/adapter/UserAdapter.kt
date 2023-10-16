package com.vr.smartreceiving.adapter
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceiving.R
import com.vr.smartreceiving.model.UserModel
import java.util.Locale


class UserAdapter(
    private var userList: MutableList<UserModel>,
    val context: Context,
    private val onEditClickListener: (UserModel) -> Unit,
    private val onHapusClickListener: (UserModel) -> Unit,
) : RecyclerView.Adapter<UserAdapter.ProductViewHolder>() {
    public var filteredBarangList: MutableList<UserModel> = mutableListOf()
    init {
        filteredBarangList.addAll(userList)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && filteredBarangList.isEmpty()) {
            1 // Return 1 for empty state view
        } else {
            0 // Return 0 for regular product view
        }
    }
    fun filter(query: String) {
        filteredBarangList.clear()
        if (query !== null || query !=="") {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (product in userList) {
                val nam = product.nama?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredBarangList.add(product)
                    Log.d("Ada ", product.nama.toString())
                }
            }
        } else {
            filteredBarangList.addAll(userList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredBarangList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredBarangList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentBarang = filteredBarangList[position]

        holder.tvNama.text = currentBarang.nama
        holder.tvUsername.text = currentBarang.username
        holder.tvPassword.text = currentBarang.password
        holder.tvNoHp.text = currentBarang.noHp
        holder.btnUbah.setOnClickListener { onEditClickListener(currentBarang) }
        holder.btnHapus.setOnClickListener { onHapusClickListener(currentBarang) }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvPassword: TextView = itemView.findViewById(R.id.tvPassword)
        val tvNoHp: TextView = itemView.findViewById(R.id.tvNoHp)
        val btnUbah: LinearLayout = itemView.findViewById(R.id.btnUbah)
        val btnHapus: LinearLayout = itemView.findViewById(R.id.btnHapus)
    }
}
