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
import com.vr.smartreceiving.model.BarangModel
import com.vr.smartreceiving.model.ReportDetailModel
import com.vr.smartreceiving.model.ReportModel
import java.util.Locale


class ReportAdapter(
    private var barangList: MutableList<ReportModel>,
    val context: Context,
    private val onHapusClickListener: (ReportModel) -> Unit,
) : RecyclerView.Adapter<ReportAdapter.ProductViewHolder>() {
    public var filteredBarangList: MutableList<ReportModel> = mutableListOf()
    init {
        filteredBarangList.addAll(barangList)
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
            for (product in barangList) {
                val nam = product.nama?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredBarangList.add(product)
                    Log.d("Ada ", product.nama.toString())
                }
            }
        } else {
            filteredBarangList.addAll(barangList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredBarangList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredBarangList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentBarang = filteredBarangList[position]

        holder.tvNama.text = "Rack : "+currentBarang.rakId
        var item = "Item ("+currentBarang.itemMerek+") "+currentBarang.itemNama
        if(currentBarang.itemJenis == ""){
            item += " - "+currentBarang.itemJenis
        }else{
            item += ""
        }
        holder.tvItemNama.text = "Item "+item
        holder.tvNomorPenerimaan.text = "Nomor Penerimaan "+currentBarang.nomorPenerimaan
        holder.tvJumlah.text = "Jumlah "+currentBarang.jumlah+" "+currentBarang.satuan
        holder.tvPetugas.text = "Petugas "+currentBarang.petugasNama
        holder.tvScanAt.text = "Scan pada "+currentBarang.createdAt
        holder.btnHapus.setOnClickListener { onHapusClickListener(currentBarang) }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvNomorPenerimaan: TextView = itemView.findViewById(R.id.tvNomorPenerimaan)
        val tvItemNama: TextView = itemView.findViewById(R.id.tvItemNama)
        val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        val tvPetugas: TextView = itemView.findViewById(R.id.tvPetugas)
        val tvScanAt: TextView = itemView.findViewById(R.id.tvScanAt)
        val btnHapus: LinearLayout = itemView.findViewById(R.id.btnHapus)
    }
}
