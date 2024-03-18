package com.rfsayago.agendamontera

import androidx.appcompat.app.AppCompatActivity

class MyToolbar {
    fun show(activity: AppCompatActivity, title: String, backButton: Boolean) {
        activity.setSupportActionBar(activity.findViewById(R.id.toolbar))
        activity.supportActionBar?.title = title
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(backButton)
    }
}