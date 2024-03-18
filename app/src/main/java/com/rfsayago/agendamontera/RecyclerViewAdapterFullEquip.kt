package com.rfsayago.agendamontera

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rfsayago.agendamontera.databinding.ItemFullEquipBinding

class RecyclerViewAdapterFullEquip(
    context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerViewAdapterFullEquip.ViewHolderFullEquip>() {

    lateinit var context: Context
    lateinit var cursor: Cursor
    lateinit var huntingDBHelper: MySQLiteHelper

    // We create the interface to manage the clicks of the items.
    interface OnItemClickListener {
        fun onDogActionClick(dogName: String)

    }


    @SuppressLint("NotConstructor")
    fun RecyclerViewAdapterFullEquip(context: Context, cursor: Cursor) {

        // We define for this object the context and the cursor that is sent to us as a parameter.
        this.context = context
        this.cursor = cursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFullEquip {
        // Inflater data.
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolderFullEquip(
            inflater.inflate(
                R.layout.item_full_equip,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolderFullEquip, position: Int) {
        // From the data it is linked to its visual part.
        // We load the data we need.
        cursor.moveToPosition(position)

        // We load the holder data with the cursor column we want.
        holder.tvId.text = (position + 1).toString()
        holder.tvName.text = cursor.getString(1)
        if (cursor.getString(3) == "0") holder.tvSelected.setImageResource(R.drawable.ic_action_unselected)
        else holder.tvSelected.setImageResource(R.drawable.ic_action_selected)
    }

    override fun getItemCount(): Int {
        // BD size if not null.
        if (cursor == null) {
            return 0
        } else return cursor.count
    }

    inner class ViewHolderFullEquip : RecyclerView.ViewHolder {

        // We define the fields that we will show.
        val tvId: TextView
        val tvName: TextView
        val tvSelected: ImageView
        val btnActions: ImageButton
        val lyRow: LinearLayout


        constructor(view: View) : super(view) {
            // We create the connection with item_full_equip.xml
            val bindingItemsRVFE = ItemFullEquipBinding.bind(view)

            // We link each field.
            tvId = bindingItemsRVFE.tvFullEquipId
            tvName = bindingItemsRVFE.tvFullEquipName
            tvSelected = bindingItemsRVFE.tvFullEquipSelected
            btnActions = bindingItemsRVFE.ibFeActions
            lyRow = bindingItemsRVFE.lyRow

            huntingDBHelper = MySQLiteHelper(context)

            btnActions.setOnClickListener {
                itemClickListener.onDogActionClick(tvName.text.toString())
            }

        }
    }
}