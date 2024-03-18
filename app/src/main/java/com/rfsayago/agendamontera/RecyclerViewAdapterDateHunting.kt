package com.rfsayago.agendamontera

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rfsayago.agendamontera.databinding.ItemDateHuntingBinding

class RecyclerViewAdapterDateHunting(
    context: Context,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerViewAdapterDateHunting.ViewHolderHunting>() {

    lateinit var context: Context
    lateinit var cursor: Cursor
    lateinit var huntingDBHelper: MySQLiteHelper

    //We create the interface to handle the clicks of the iitemClickListenertems.
    interface OnItemClickListener {
        fun onHuntingActionClick(dogName: String)

    }


    @SuppressLint("NotConstructor")
    fun RecyclerViewAdapterDateHunting(context: Context, cursor: Cursor) {

        // We define for this object the context and the cursor that is sent to us as a parameter.
        this.context = context
        this.cursor = cursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHunting {
        // Inflater data.
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolderHunting(
            inflater.inflate(
                R.layout.item_date_hunting,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolderHunting, position: Int) {
        // From the data it is linked to its visual part.
        // We load the data we need.
        cursor.moveToPosition(position)

        // We load the holder data with the cursor column we want.
        holder.tvId.text = (position + 1).toString()
        holder.tvDate.text = cursor.getString(0)
        holder.tvHunting.text = cursor.getString(1)
    }

    override fun getItemCount(): Int {
        // BD size if not null.
        if (cursor == null) {
            return 0
        } else return cursor.count
    }

    inner class ViewHolderHunting : RecyclerView.ViewHolder {

        // We define the fields that we will show.
        val tvId: TextView
        val tvDate: TextView
        val tvHunting: TextView
        val btnActions: ImageButton
        val lyRow: LinearLayout


        constructor(view: View) : super(view) {
            // We create the connection with item_date_hunting.xml
            val bindingItemsRVFE = ItemDateHuntingBinding.bind(view)

            // We link each field.
            tvId = bindingItemsRVFE.tvHuntingId
            tvDate = bindingItemsRVFE.tvHuntingDate
            tvHunting = bindingItemsRVFE.tvHunting
            btnActions = bindingItemsRVFE.ibDHActions
            lyRow = bindingItemsRVFE.lyRowHunting

            huntingDBHelper = MySQLiteHelper(context)

            btnActions.setOnClickListener {
                itemClickListener.onHuntingActionClick(tvHunting.text.toString())
            }

        }
    }
}
