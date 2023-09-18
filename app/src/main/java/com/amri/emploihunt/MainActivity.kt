package com.amri.emploihunt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import com.amri.emploihunt.databinding.ActivityMainBinding
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner
import ru.embersoft.expandabletextview.ExpandableTextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.row_campus_placement)
        
    }

}


/*
<ru.embersoft.expandabletextview.ExpandableTextView
android:id="@+id/campusAddress"

android:layout_width="0dp"
android:layout_height="wrap_content"
app:etv_animationTime="10"
app:etv_expandLines="3"
app:etv_showLine="false"
app:isExpanded="true"
app:etv_textContentColor="@color/black"
app:etv_textContentSize="15sp"
app:etv_textExpand="show more"
app:etv_textShrink="Show less"
app:etv_textStateColor="#9F000000"

app:layout_constraintTop_toBottomOf="@+id/collegeName"
app:layout_constraintStart_toStartOf="parent"
app:layout_constraintEnd_toEndOf="parent"
app:layout_constraintBottom_toBottomOf="parent"
android:layout_marginTop="@dimen/_10sdp" />*/
