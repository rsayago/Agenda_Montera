package com.rfsayago.agendamontera

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.rfsayago.agendamontera.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyToolbar().show(this, "Agenda Montera", false)

        initListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        binding.actFullEquip.setOnClickListener {
            val intent = Intent(this, FullEquipActivity::class.java)
            startActivity(intent)
        }
        binding.actDogList.setOnClickListener {
            val intent = Intent(this, DogListActivity::class.java)
            startActivity(intent)
        }
        binding.actCollars.setOnClickListener {
            val intent = Intent(this, CollarsActivity::class.java)
            startActivity(intent)
        }
        binding.actAgenda.setOnClickListener {
            val intent = Intent(this, DateHuntingActivity::class.java)
            startActivity(intent)
        }

        binding.ibExit.setOnClickListener{
            val dialogConfirm = Dialog(this)
            dialogConfirm.setContentView(R.layout.dialog_confirm)
            val btnYes: Button = dialogConfirm.findViewById(R.id.btnOptionYes)
            val btnNo: Button = dialogConfirm.findViewById(R.id.btnOptionNo)
            val tvConfirm: TextView = dialogConfirm.findViewById(R.id.tvConfirm)
            val lyConfirm: LinearLayout = dialogConfirm.findViewById(R.id.lyConfirm)

            // Confirm dialog.
            dialogConfirm.show()

            tvConfirm.text = "¿Estás seguro que quieres SALIR?"
            lyConfirm.setBackgroundResource(R.mipmap.antonio_nieve)

            // Buttons options.
            btnYes.setOnClickListener {
                dialogConfirm.hide()
                finish()
            }

            btnNo.setOnClickListener {
                dialogConfirm.hide()
            }

        }
    }


}