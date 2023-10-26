package com.matvienko.taigergpstracker.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.matvienko.taigergpstracker.DB.TrackItem
import com.matvienko.taigergpstracker.R
import com.matvienko.taigergpstracker.databinding.SaveDialogBinding

object DialogManager {
    fun showLocEnableDialog (context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle(R.string.location_disabled_massage)
        dialog.setMessage(context.getString(R.string.location_disabled_massage))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { _, _ ->
            listener.onClick()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    fun showSaveDialog (context: Context, item: TrackItem, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val binding = SaveDialogBinding.inflate(
            LayoutInflater.from(context), null, false)
        builder.setView(binding.root)
        val dialog = builder.create()
        binding.apply {
            tvTime.text = "${item.time} min"
            tvSpeed.text = "Speed: ${item.velocity} km/h"
            tvDistance.text = "Distance: ${item.distance} km"
            bSave.setOnClickListener{
                listener.onClick()
                dialog.dismiss()
            }
            bCancel.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //сделать фон кард вью прозрачным
        dialog.show()
    }
    interface Listener {
        fun onClick() {

        }
    }
}