package com.amri.emploihunt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import com.amri.emploihunt.databinding.ActivityMainBinding
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.spSearchable.setSearchDialogGravity(Gravity.TOP)
        binding.spSearchable.arrowPaddingRight = 19
        binding.spSearchable.item = resources.getStringArray(R.array.degree_array).toList()
        binding.spSearchable.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {

                binding.spSearchable.errorText = "Your selected item is \"" + binding.spSearchable.item[position] + "\" ."

            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                binding.spSearchable.errorText = "On Nothing Selected"
            }
        }
    }
}