package com.rfsayago.agendamontera

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rfsayago.agendamontera.databinding.ActivityFullEquipBinding

class FullEquipActivity : AppCompatActivity(), RecyclerViewAdapterFullEquip.OnItemClickListener {
    private lateinit var binding: ActivityFullEquipBinding
    private lateinit var huntingDBHelper: MySQLiteHelper
    private lateinit var recyclerViewAdapterFullEquip: RecyclerViewAdapterFullEquip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullEquipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().show(this, "El Equipo Completo", true)

        huntingDBHelper = MySQLiteHelper(this)
        initRecyclerView()
        initListeners()


    }

    private fun initRecyclerView() {
        updateFullEquip()
    }


    private fun initListeners() {
        binding.fabFullEquipAddDog.setOnClickListener { showDialogAddDog() }
    }

    private fun showDialogAddDog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_dog)

        var btnAddDog: Button = dialog.findViewById(R.id.btnAddDog)
        var etAddDog: EditText = dialog.findViewById(R.id.etAddDog)

        btnAddDog.setOnClickListener {
            var newDog = etAddDog.text.toString().trim()
            if (newDog.isNotBlank() && newDog.isNotEmpty()) {
                if (huntingDBHelper.addDog(newDog.uppercase())) {
                    updateFullEquip()
                    dialog.hide()


                } else {
                    Toast.makeText(this, "Ese perro ya existe en su equipo", Toast.LENGTH_SHORT)
                        .show()
                    etAddDog.text.clear()
                }

            } else {
                Toast.makeText(this, "No puede estar vacío", Toast.LENGTH_SHORT).show()
                dialog.hide()
            }
        }

        dialog.show()
    }


    private fun updateFullEquip() {
        // We query the database.
        var query = "SELECT * FROM ${MySQLiteHelper.TABLE_DOGS} ORDER BY ${MySQLiteHelper.FIELD_NAME}"
        val cursor: Cursor = huntingDBHelper.execQuery(query)!!

        // We pass it to the Full Equip adapter with the interface.
        recyclerViewAdapterFullEquip = RecyclerViewAdapterFullEquip(this, this)
        // We pass our adapter the context and the cursor.
        recyclerViewAdapterFullEquip.RecyclerViewAdapterFullEquip(this, cursor)

        // We bind our recycler view by its id.
        binding.rvFullEquip.setHasFixedSize(true)
        binding.rvFullEquip.layoutManager = LinearLayoutManager(this)
        // We put division lines in the Recyclerview.
        binding.rvFullEquip.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.rvFullEquip.adapter = recyclerViewAdapterFullEquip

        // Update count dogs.
        countDogs()
        // Update selecteds dogs
        selectedDogs()


    }

    private fun countDogs() {
        val countDogs: Long = huntingDBHelper.allDogs()
        val numDogs = countDogs.toString()
        val totalDogs: String = getString(R.string.total_dogs) + " $numDogs"
        binding.tvTotalDogs.text = totalDogs
    }

    private fun selectedDogs() {
        // We query the database.
        var query =
            "SELECT * FROM ${MySQLiteHelper.TABLE_DOGS} WHERE ${MySQLiteHelper.FIELD_SELECTED} = 1"
        val cursor: Cursor = huntingDBHelper.execQuery(query)!!
        val numDogs = cursor.count.toString()
        val totalDogs: String = getString(R.string.total_selected) + " $numDogs"
        binding.tvTotalSelected.text = totalDogs
    }

    override fun onDogActionClick(dogName: String) {
        showActionsDialog(this, dogName)
    }

    @SuppressLint("SetTextI18n")
    private fun showActionsDialog(context: Context, dogName: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_action_full_equip)
        val tvAction: TextView = dialog.findViewById(R.id.tvActionFE)
        val btnAction: Button = dialog.findViewById(R.id.btnActionOption)
        val rgAction: RadioGroup = dialog.findViewById(R.id.rgActionFE)

        val text: String = tvAction.hint.toString()
        tvAction.hint = "$text $dogName"
        // Change the text of the radio button depending on the value it had.
        dialog.show()
        var query = "SELECT * " +
                "FROM ${MySQLiteHelper.TABLE_DOGS} " +
                "WHERE ${MySQLiteHelper.FIELD_NAME} = '$dogName'"
        val cursor = huntingDBHelper.execQuery(query)
        cursor?.moveToFirst()
        val isSelected = cursor?.getInt(3)!! > 0

        if (isSelected) {
            // We load the radio button.
            val rbAddList = dialog.findViewById<RadioButton>(R.id.rbAddToList)
            //We change the text with a string of the strings.
            rbAddList.text = context.getString(R.string.dialog_action_remove_list)
        }

        btnAction.setOnClickListener {

            val selectedId = rgAction.checkedRadioButtonId
            val selectedRadioButton: RadioButton = rgAction.findViewById(selectedId)
            when (selectedRadioButton.tag) {
                "add" -> {
                    dialog.hide()
                    val dialogConfirm = Dialog(context)
                    dialogConfirm.setContentView(R.layout.dialog_confirm)
                    val btnYes: Button = dialogConfirm.findViewById(R.id.btnOptionYes)
                    val btnNo: Button = dialogConfirm.findViewById(R.id.btnOptionNo)
                    val tvConfirm: TextView = dialogConfirm.findViewById(R.id.tvConfirm)

                    // Confirm dialog
                    dialogConfirm.show()

                    if (!isSelected) {
                        tvConfirm.text = "¿Estás seguro que quieres AÑADIR a $dogName a la lista?"
                    } else tvConfirm.text =
                        "¿Estás seguro que quieres QUITAR a $dogName de la lista?"

                    // Buttons options.
                    btnYes.setOnClickListener {

                        val selected = huntingDBHelper.updateSelected(dogName)

                        if (selected == null) {
                            dialog.hide()
                            Toast.makeText(context, "Ha ocurrido un error", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            updateFullEquip()
                            selectedDogs()
                            dialogConfirm.hide()
                        }

                    }

                    btnNo.setOnClickListener {
                        dialogConfirm.hide()
                    }
                }

                "update" -> {
                    dialog.hide()
                    actionUpdateDog(context, dogName)
                }

                "delete" -> {
                    huntingDBHelper.deleteDog(dogName)
                    updateFullEquip()
                    // Update count dogs.
                    countDogs()
                    // Update selecteds dogs.
                    selectedDogs()
                    dialog.hide()
                }

                else -> dialog.hide()
            }
        }
    }

    private fun actionUpdateDog(context: Context, dogName: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_update_dog)

        var btnUpdateDog: Button = dialog.findViewById(R.id.btnUpdateDog)
        var etUpdateDog: EditText = dialog.findViewById(R.id.etUpdateDog)
        etUpdateDog.setText(dogName)

        btnUpdateDog.setOnClickListener {
            var updateDog = etUpdateDog.text.toString().trim()
            if (updateDog.isNotBlank() && updateDog.isNotEmpty()) {

                if (huntingDBHelper.updateDogName(dogName, updateDog.uppercase())) {
                    updateFullEquip()
                    dialog.hide()
                } else {
                    Toast.makeText(context, "Error al modificar el nombre", Toast.LENGTH_SHORT)
                        .show()
                }

            } else {
                Toast.makeText(context, "No puede estar vacío", Toast.LENGTH_SHORT).show()
                dialog.hide()
            }
        }
        dialog.show()
    }
}