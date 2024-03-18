package com.rfsayago.agendamontera

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rfsayago.agendamontera.databinding.ActivityCollarsBinding
import java.text.DecimalFormat

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityCollarsBinding
private lateinit var huntingDBHelper: MySQLiteHelper

class CollarsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().show(this, "Collares GPS", true)

        huntingDBHelper = MySQLiteHelper(this)


        initUi()

        initListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun initUi() {
        val numCollars = numCollars()
        val initCollars = initCollars()
        if (numCollars > 0) {
            binding.tvResume.text = "Tienes $numCollars Collares y empiezan en el número $initCollars"
            binding.tvHowManyCollars.text=numCollars.toString()
            binding.tvNumCollars.text=initCollars.toString()

            // We move the slider bars to the appropriate positions.
            // For the number of collars.
            val totalCollarsValue = numCollars
            var lmTotalCollars = mutableListOf(totalCollarsValue.toFloat())
            lmTotalCollars.also { binding.rsHowManyCollars.values = it }

            //For the beginning of the collars numbering.
            val initCollarsValue = initCollars
            var lmNumCollars = mutableListOf(initCollarsValue.toFloat())
            lmNumCollars.also { binding.rsNumCollars.values = it }
        }
    }

    private fun numCollars(): Int {
        // Total collars.
        val query = "SELECT * FROM ${MySQLiteHelper.TABLE_COLLARS_GPS}"

        val cursor = huntingDBHelper.execQuery(query)
        return cursor?.count ?: 0
    }

    private fun initCollars(): Int {
        // Begining of de collars numbering.
        val query = "SELECT * FROM ${MySQLiteHelper.TABLE_COLLARS_GPS}"

        val cursor = huntingDBHelper.execQuery(query)

        val first = cursor?.moveToFirst()

        return if(first == null || !first) {
            0
        } else cursor.getInt(0)

    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        binding.rsHowManyCollars.addOnChangeListener { _, value, _ ->
            val df = DecimalFormat("#.##")
            val resultNumCollars = df.format(value)
            binding.tvHowManyCollars.text = resultNumCollars
        }
        binding.rsNumCollars.addOnChangeListener { _, value, _ ->
            val df = DecimalFormat("#.##")
            val resultInitCollars = df.format(value)
            binding.tvNumCollars.text = resultInitCollars
        }
        binding.btbCreateCollars.setOnClickListener{
            val dialogConfirm = Dialog(this)
            dialogConfirm.setContentView(R.layout.dialog_confirm)
            val btnYes: Button = dialogConfirm.findViewById(R.id.btnOptionYes)
            val btnNo: Button = dialogConfirm.findViewById(R.id.btnOptionNo)
            val tvConfirm: TextView = dialogConfirm.findViewById(R.id.tvConfirm)

            val query = "SELECT * FROM ${MySQLiteHelper.TABLE_COLLARS_GPS}"

            val cursor = huntingDBHelper.execQuery(query)

            dialogConfirm.show()

            val numCollars = binding.tvHowManyCollars.text.toString()
            val initCollars = binding.tvNumCollars.text.toString()
            if ((cursor != null) && cursor.moveToFirst()) {
                tvConfirm.text = "Esto eliminará los collares que tienes crados y creará collares nuevos ¿Estás seguro?"
            } else tvConfirm.text = "¿Vas a crear $numCollars collares, empezando en el número $initCollars?"

            // Buttons options.
            btnYes.setOnClickListener {
                huntingDBHelper.createCollars(numCollars.toInt(), initCollars.toInt())
                dialogConfirm.hide()
                initUi()
            }

            btnNo.setOnClickListener {
                dialogConfirm.hide()
                Toast.makeText(this, "Operación Cancelada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}