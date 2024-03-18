package com.rfsayago.agendamontera

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rfsayago.agendamontera.databinding.ActivityDogListBinding

class DogListActivity : AppCompatActivity(), RecyclerViewAdapterListEquip.OnItemClickListener {

    private lateinit var binding: ActivityDogListBinding
    private lateinit var huntingDBHelper: MySQLiteHelper
    private lateinit var recyclerViewAdapterListEquip: RecyclerViewAdapterListEquip
    lateinit var selectedItem: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().show(this, "Lista de Seleccionados", true)

        huntingDBHelper = MySQLiteHelper(this)

        initRecyclerView()


    }

    private fun initRecyclerView() {
        updateListEquip()
    }

    @SuppressLint("SetTextI18n")
    fun updateListEquip() {
        // We query the database.
        var query =
            "SELECT * FROM ${MySQLiteHelper.TABLE_DOGS} WHERE ${MySQLiteHelper.FIELD_SELECTED} = 1 ORDER BY name"
        val cursor: Cursor = huntingDBHelper.execQuery(query)!!

        // We pass it to the Full Equip adapter with the interface.
        recyclerViewAdapterListEquip = RecyclerViewAdapterListEquip(this, this)
        // We pass our adapter the context and the cursor.
        recyclerViewAdapterListEquip.RecyclerViewAdapterListEquip(this, cursor)

        // We bind our recycler view by its id.
        binding.rvListEquip.setHasFixedSize(true)
        binding.rvListEquip.layoutManager = LinearLayoutManager(this)
        // We put division lines in the Recyclerview.
        binding.rvListEquip.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.rvListEquip.adapter = recyclerViewAdapterListEquip

        // Update count dogs.
        val countDog = countDogs()
        // Update selecteds dogs
        val missingDog = collectedDogs()
        if (missingDog == 0 && countDog > 0){
            val gooDialog = Dialog(this)
            gooDialog.setContentView(R.layout.good_job)
            val okBtn: Button = gooDialog.findViewById(R.id.btnGoodJob)
            okBtn.setOnClickListener{
                gooDialog.hide()
                val cleanDialog = Dialog(this)
                cleanDialog.setContentView(R.layout.dialog_confirm)
                val btnYes: Button = cleanDialog.findViewById(R.id.btnOptionYes)
                val btnNo: Button = cleanDialog.findViewById(R.id.btnOptionNo)
                val tvConfirm: TextView = cleanDialog.findViewById(R.id.tvConfirm)

                tvConfirm.text = "¿Quieres borrar esta lista de perros?"

                cleanDialog.show()

                // Buttons options.
                btnYes.setOnClickListener {
                    huntingDBHelper.clearList()
                    updateListEquip()
                    cleanDialog.hide()
                }

                btnNo.setOnClickListener {
                    cleanDialog.hide()
                }
            }
            gooDialog.show()
        }

    }

    private fun countDogs(): Int {
        val countDogs: Long = huntingDBHelper.allListDogs()
        val numDogs = countDogs.toString()
        val totalDogs: String = getString(R.string.total_dogs) + " $numDogs"
        binding.tvLeTotalDogs.text = totalDogs
        return numDogs.toInt()
    }

    private fun collectedDogs(): Int {
        // We query the database.
        var query =
            "SELECT * FROM ${MySQLiteHelper.TABLE_DOGS} WHERE ${MySQLiteHelper.FIELD_COLLECTED} = 1"
        val cursor: Cursor = huntingDBHelper.execQuery(query)!!
        val collectedDogs = cursor.count.toString()
        val totalDogs: String = getString(R.string.total_collected) + " $collectedDogs"
        binding.tvLeTotalCollected.text = totalDogs
        val countDogs: Long = huntingDBHelper.allListDogs()
        val missingDogsInt = countDogs.toInt() - collectedDogs.toInt()
        val missingDogs = getString(R.string.total_missing) + " " + missingDogsInt.toString()
        binding.tvLeMissing.text = missingDogs
        return missingDogsInt
    }

    override fun onDogActionClick(dogName: String) {
        showActionsDialog(this, dogName)
    }

    @SuppressLint("SetTextI18n")
    private fun showActionsDialog(context: Context, dogName: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_action_list_equip)
        val tvAction: TextView = dialog.findViewById(R.id.tvActionLE)
        val btnAction: Button = dialog.findViewById(R.id.btnActionOptionLE)
        val rgAction: RadioGroup = dialog.findViewById(R.id.rgActionLE)

        val text: String = tvAction.hint.toString()
        tvAction.hint = "$text $dogName"
        // Change the text of the radio button depending on the value it had.
        dialog.show()
        var query = "SELECT * " +
                "FROM ${MySQLiteHelper.TABLE_DOGS} " +
                "WHERE ${MySQLiteHelper.FIELD_NAME} = '$dogName'"
        val cursor = huntingDBHelper.execQuery(query)
        cursor?.moveToFirst()
        val isSelected = cursor?.getInt(4)!! > 0

        if (isSelected) {
            // We load the radio button.
            val rbRecollected = dialog.findViewById<RadioButton>(R.id.rbCollected)
            //We change the text with a string of the strings.
            rbRecollected.text = context.getString(R.string.dialog_action_no_collected_dog)
        }
        btnAction.setOnClickListener {

            val selectedId = rgAction.checkedRadioButtonId
            val selectedRadioButton: RadioButton = rgAction.findViewById(selectedId)
            when (selectedRadioButton.tag) {
                "collected" -> {
                    dialog.hide()
                    val dialogConfirm = Dialog(context)
                    dialogConfirm.setContentView(R.layout.dialog_confirm)
                    val btnYes: Button = dialogConfirm.findViewById(R.id.btnOptionYes)
                    val btnNo: Button = dialogConfirm.findViewById(R.id.btnOptionNo)
                    val tvConfirm: TextView = dialogConfirm.findViewById(R.id.tvConfirm)

                    dialogConfirm.show()

                    if (!isSelected) {
                        tvConfirm.text = "¿Estás seguro que has recogido a $dogName?"
                    } else tvConfirm.text =
                        "¿Estás seguro que NO has recogido a $dogName?"

                    // Buttons options.
                    btnYes.setOnClickListener {

                        val collected = huntingDBHelper.updateCollected(dogName)

                        if (collected == null) {
                            dialog.hide()
                            Toast.makeText(context, "Ha ocurrido un error", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            updateListEquip()
                            dialogConfirm.hide()
                        }

                    }

                    btnNo.setOnClickListener {
                        dialogConfirm.hide()
                    }
                }

                "gps" -> {
                    dialog.hide()
                    val dialogGps = Dialog(context)
                    dialogGps.setContentView(R.layout.spinner_gps)
                    val spinner: Spinner = dialogGps.findViewById(R.id.spinnerGps)
                    //We add the dog's name to the text.
                    val tvGps: TextView = dialogGps.findViewById(R.id.tvGps)
                    val text = tvGps.text.toString() + dogName
                    tvGps.text = text
                    //We generate the list of collars.
                    val query =
                        "SELECT ${MySQLiteHelper.FIELD_COLLAR_NUMBER} FROM ${MySQLiteHelper.TABLE_COLLARS_GPS} WHERE ${MySQLiteHelper.FIELD_USED} == 0"
                    val cursor = huntingDBHelper.execQuery(query)
                    var items = arrayListOf("NO")
                    if (cursor != null) {
                        val list = generateSequence { if (cursor.moveToNext()) cursor else null }
                            .map { cursor.getInt(0) }
                            .toList()
                        for (item in list) {
                            items.add(item.toString())
                        }
                    }

                    dialogGps.findViewById<Button>(R.id.btnAssignCollar).setOnClickListener {
                        // We assign the collar and update the list
                        huntingDBHelper.assignCollars(dogName, selectedItem)
                        updateListEquip()
                        dialogGps.hide()
                    }


                    val adpter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
                    //Select the design of the drop-down menu.
                    adpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    //We assign the adapter to our spinner.
                    spinner.adapter = adpter

                    dialogGps.show()

                    //We create a method to see which one is selected.
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedItem = items[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }
                }


                else -> dialog.hide()
            }
        }

    }
}