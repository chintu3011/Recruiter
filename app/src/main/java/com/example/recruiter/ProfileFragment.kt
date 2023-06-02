package com.example.recruiter

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.imageview.ShapeableImageView

class ProfileFragment : Fragment() {
    lateinit var editbasic : ShapeableImageView
    lateinit var editabout : ShapeableImageView
    lateinit var editexp : ShapeableImageView
    lateinit var editresume : ShapeableImageView
    lateinit var editjobpref : ShapeableImageView
    lateinit var fragview : View
    lateinit var alertDialog: AlertDialog; lateinit var alertbasic : AlertDialog
    lateinit var alertexp : AlertDialog; lateinit var alertresume : AlertDialog
    lateinit var alertjob : AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragview = inflater.inflate(R.layout.fragment_profile, container, false)
        val basicdialogView = inflater.inflate(R.layout.dialog_profile_basic_info,null)
        val aboutdialogView = inflater.inflate(R.layout.dialog_about_info,null)
        val expdialogView = inflater.inflate(R.layout.dialog_experience_info,null)
        val resumedialogView = inflater.inflate(R.layout.dialog_resume_info,null)
        val jobprefdialogView = inflater.inflate(R.layout.dialog_job_preference_info,null)
        editabout = fragview.findViewById(R.id.changeBackImg)
        editbasic = fragview.findViewById(R.id.editBioJ)
        editexp = fragview.findViewById(R.id.editExperienceJ)
        editresume = fragview.findViewById(R.id.editResumeJ)
        editjobpref = fragview.findViewById(R.id.editJobPrefJ)
        editabout.setOnClickListener {
             alertDialog = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
                .setView(basicdialogView)
                 .setTitle("Change Info")
                 .setPositiveButton("Done") { dialog, _ ->
                     Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                     dialog.dismiss()
                 }
                 .setNegativeButton("Cancel"){ dialog, _ ->
                     dialog.dismiss()
                 }
                .create()
            alertDialog.show()
        }
        editbasic.setOnClickListener {
            alertbasic = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
                .setView(aboutdialogView)
                .setTitle("Change Basics")
                .setPositiveButton("Done") { dialog, _ ->
                    Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertbasic.show()
        }
        editexp.setOnClickListener {
            alertexp = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
                .setView(expdialogView)
                .setTitle("Change Info")
                .setPositiveButton("Done") { dialog, _ ->
                    Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertexp.show()
        }
        editresume.setOnClickListener {
            alertresume = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
                .setView(resumedialogView)
                .setTitle("Change Info")
                .setPositiveButton("Done") { dialog, _ ->
                    Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertresume.show()
        }
        editjobpref.setOnClickListener {
            alertjob = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
                .setView(jobprefdialogView)
                .setTitle("Change Info")
                .setPositiveButton("Done") { dialog, _ ->
                    Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertjob.show()
        }
        return fragview
    }

}