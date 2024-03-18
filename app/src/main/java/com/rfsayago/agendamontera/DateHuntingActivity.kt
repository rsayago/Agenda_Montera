package com.rfsayago.agendamontera

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.rfsayago.agendamontera.databinding.ActivityDateHuntingBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateHuntingActivity : AppCompatActivity() , RecyclerViewAdapterDateHunting.OnItemClickListener{

    private lateinit var binding: ActivityDateHuntingBinding
    private lateinit var huntingDBHelper: MySQLiteHelper
    private lateinit var recyclerViewAdapterDateHunting: RecyclerViewAdapterDateHunting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateHuntingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().show(this, "Listado de Monterias", true)
        huntingDBHelper = MySQLiteHelper(this)
        initRecyclerView()
        initListeners()

    }

    private fun initRecyclerView() {
        updateHunting()
    }


    private fun initListeners() {
        binding.fabHDAddHunting.setOnClickListener { showDialogAddHunting() }
    }

    private fun showDialogAddHunting() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_hunting)

        var btnAddHunting: Button = dialog.findViewById(R.id.btnAddHunting)
        var etAddHunting: EditText = dialog.findViewById(R.id.etAddHunting)


        //We create datepicker and its functionality.
        // We change the language to Spanish.
        val locale = Locale("es", "ES")
        Locale.setDefault(locale)
        val calendar = Calendar.getInstance(locale)

        val dateHuntingPicker = DatePickerDialog.OnDateSetListener{ _, year, month, day->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            // We create the date format.
            val dateFormat = "dd-MM-yyyy"
            val simpleFormat = SimpleDateFormat(dateFormat, Locale.ENGLISH)
            // We assign the date from the datepicker to the textView.
            val fieldDate = dialog.findViewById<TextView>(R.id.tvDateField)
            fieldDate.text = simpleFormat.format(calendar.time)
        }


        val btnSelectDate = dialog.findViewById<Button>(R.id.btnSelectDate)
        btnSelectDate.setOnClickListener{
            DatePickerDialog(
                this,
                dateHuntingPicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()

        }

        btnAddHunting.setOnClickListener {
            var newHunting = etAddHunting.text.toString().trim()
            val fieldDate = dialog.findViewById<TextView>(R.id.tvDateField)
            val dateHunting = fieldDate.text.toString()
            if (newHunting.isNotBlank() && newHunting.isNotEmpty() && dateHunting != "dd/mm/aaaa") {
                if (huntingDBHelper.addHunting(newHunting.uppercase(), dateHunting)) {
                    updateHunting()
                    dialog.hide()


                } else {
                    Toast.makeText(this, "No puede haber 2 monterías con el mismo nombre", Toast.LENGTH_SHORT)
                        .show()
                    etAddHunting.text.clear()
                }

            } else {
                Toast.makeText(this, "Hay que rellenar Nombre y Fecha", Toast.LENGTH_SHORT).show()
                dialog.hide()
            }
        }

        dialog.show()
    }

    private fun updateHunting() {
        // We query the database.
        var query = "SELECT * FROM ${MySQLiteHelper.TABLE_HUNTING_DATES} ORDER BY ${MySQLiteHelper.FIELD_DATE}"
        val cursor: Cursor = huntingDBHelper.execQuery(query)!!

        // We pass it to the Hunting Date adapter with the interface.
        recyclerViewAdapterDateHunting = RecyclerViewAdapterDateHunting(this, this)
        // We pass our adapter the context and the cursor.
        recyclerViewAdapterDateHunting.RecyclerViewAdapterDateHunting(this, cursor)

        // We bind our recycler view by its id.
        binding.rvHuntingDate.setHasFixedSize(true)
        binding.rvHuntingDate.layoutManager = LinearLayoutManager(this)
        // We put division lines in the Recyclerview.
        binding.rvHuntingDate.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.rvHuntingDate.adapter = recyclerViewAdapterDateHunting

        // Update count Hunting.
        countHunting()
    }
    private fun countHunting() {
        val countDogs: Long = huntingDBHelper.allHunting()
        val numHunting = countDogs.toString()
        val totalHunting: String = getString(R.string.total_hunting) + " $numHunting"
        binding.tvHDTotalHunting.text = totalHunting
    }

    override fun onHuntingActionClick(hunting: String) {
        showActionsDialog(this, hunting)
    }

    @SuppressLint("SetTextI18n")
    private fun showActionsDialog(context: Context, hunting: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_action_hunting_date)
        val tvAction: TextView = dialog.findViewById(R.id.tvActionHD)
        val btnAction: Button = dialog.findViewById(R.id.btnActionOption)
        val rgAction: RadioGroup = dialog.findViewById(R.id.rgActionHD)

        val text: String = tvAction.hint.toString()
        tvAction.hint = "$text $hunting"
        // Change the text of the radio button depending on the value it had
        dialog.show()

        btnAction.setOnClickListener {

            val selectedId = rgAction.checkedRadioButtonId
            val selectedRadioButton: RadioButton = rgAction.findViewById(selectedId)
            when (selectedRadioButton.tag) {
                "update" -> {
                    dialog.hide()
                    actionUpdateHunting(context, hunting)
                }

                "delete" -> {
                    val dialogConfirm = Dialog(this)
                    dialogConfirm.setContentView(R.layout.dialog_confirm)
                    val btnYes: Button = dialogConfirm.findViewById(R.id.btnOptionYes)
                    val btnNo: Button = dialogConfirm.findViewById(R.id.btnOptionNo)
                    val tvConfirm: TextView = dialogConfirm.findViewById(R.id.tvConfirm)
                    val lyConfirm: LinearLayout = dialogConfirm.findViewById(R.id.lyConfirm)
                    // Confirm dialog
                    dialogConfirm.show()

                    tvConfirm.text = "¿Estás seguro que quieres eliminar la montería '$hunting'?"

                    // Buttons options.
                    btnYes.setOnClickListener {
                        dialogConfirm.hide()
                        huntingDBHelper.deleteHunting(hunting)
                        updateHunting()
                        // Update count Hunting.
                        countHunting()
                        dialog.hide()
                    }

                    btnNo.setOnClickListener {
                        dialogConfirm.hide()
                    }

                }

                else -> dialog.hide()
            }
        }
    }
    private fun actionUpdateHunting(context: Context, hunting: String){
        val query = "SELECT * FROM ${MySQLiteHelper.TABLE_HUNTING_DATES} WHERE ${MySQLiteHelper.FIELD_HUNTING_SITE} = '$hunting'"
        val cursor = huntingDBHelper.execQuery(query)

        if(cursor !=null && cursor.moveToFirst()){
            cursor.moveToFirst()
            val huntingDate = cursor.getString(0)

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_add_hunting)

            var btnAddHunting: Button = dialog.findViewById(R.id.btnAddHunting)
            var etAddHunting: EditText = dialog.findViewById(R.id.etAddHunting)
            val fieldDate = dialog.findViewById<TextView>(R.id.tvDateField)

            etAddHunting.setText(hunting)
            fieldDate.text = huntingDate
            btnAddHunting.text = "MODIFICAR"


            //We create datepicker and its functionality.
            // We change the language to Spanish.
            val locale = Locale("es", "ES")
            Locale.setDefault(locale)
            val calendar = Calendar.getInstance(locale)

            val dateHuntingPicker = DatePickerDialog.OnDateSetListener{ _, year, month, day->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                // We create the date format.
                val dateFormat = "dd-MM-yyyy"
                val simpleFormat = SimpleDateFormat(dateFormat, Locale.ENGLISH)
                // We assign the date from the datepicker to the texetView.

                fieldDate.text = simpleFormat.format(calendar.time)
            }


            val btnSelectDate = dialog.findViewById<Button>(R.id.btnSelectDate)
            btnSelectDate.setOnClickListener{
                DatePickerDialog(
                    this,
                    dateHuntingPicker,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()

            }

            btnAddHunting.setOnClickListener {
                var newHunting = etAddHunting.text.toString().trim()
                val dateHunting = fieldDate.text.toString()
                if (newHunting.isNotBlank() && newHunting.isNotEmpty() && dateHunting != "dd/mm/aaaa") {
                    if (huntingDBHelper.updateHunting(hunting, newHunting.uppercase(), dateHunting)) {
                        updateHunting()
                        dialog.hide()


                    } else {
                        Toast.makeText(this, "Hubo un error", Toast.LENGTH_SHORT)
                            .show()
                    }

                } else {
                    Toast.makeText(this, "Hay que rellenar Nombre y Fecha", Toast.LENGTH_SHORT).show()
                    dialog.hide()
                }
            }

            dialog.show()

        }
    }
}