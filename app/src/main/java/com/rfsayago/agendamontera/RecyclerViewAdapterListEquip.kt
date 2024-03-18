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
import com.rfsayago.agendamontera.databinding.ItemListEquipBinding

class RecyclerViewAdapterListEquip(
    context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerViewAdapterListEquip.ViewHolderFullEquip>() {

    lateinit var context: Context
    lateinit var cursor: Cursor
    lateinit var huntingDBHelper: MySQLiteHelper

    //We create the interface to manage the clicks of the items.
    interface OnItemClickListener {
        fun onDogActionClick(dogName: String)

    }


    @SuppressLint("NotConstructor")
    fun RecyclerViewAdapterListEquip(context: Context, cursor: Cursor) {

        // We define for this object the context and the cursor that is sent to us as a parameter.
        this.context = context
        this.cursor = cursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFullEquip {
        // Inflater data.
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolderFullEquip(
            inflater.inflate(
                R.layout.item_list_equip,
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
        holder.tvGps.text = cursor.getString(2)
        if (cursor.getString(4) == "0") holder.tvCollected.setImageResource(R.drawable.ic_action_unselected)
        else holder.tvCollected.setImageResource(R.drawable.ic_action_selected)
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
        val tvGps: TextView
        val tvCollected: ImageView
        val btnActions: ImageButton
        val lyRow: LinearLayout


        constructor(view: View) : super(view) {
            // We create the connection with item_list_equip.xml
            val bindingItemsRVLE = ItemListEquipBinding.bind(view)

            // We link each field.
            tvId = bindingItemsRVLE.tvListEquipId
            tvName = bindingItemsRVLE.tvListEquipName
            tvGps = bindingItemsRVLE.tvListEquipGps
            tvCollected = bindingItemsRVLE.tvListEquipCollected
            btnActions = bindingItemsRVLE.ibLeActions
            lyRow = bindingItemsRVLE.lyLeRow

            huntingDBHelper = MySQLiteHelper(context)

            btnActions.setOnClickListener {
                itemClickListener.onDogActionClick(tvName.text.toString())
            }

        }
    }
}