package com.vr.smartreceiving.activity.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.github.hamzaahmedkhan.spinnerdialog.callbacks.OnSpinnerOKPressedListener
import com.github.hamzaahmedkhan.spinnerdialog.enums.SpinnerSelectionType
import com.github.hamzaahmedkhan.spinnerdialog.models.SpinnerModel
import com.github.hamzaahmedkhan.spinnerdialog.ui.SpinnerDialogFragment
import com.vr.smartreceiving.R

class UserActivity : AppCompatActivity() {
    private lateinit var btnFederal :CardView
    private lateinit var btnFdr :CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        initView()
        initListener()

    }
    private fun initView(){
        btnFederal = findViewById(R.id.btnFederal)
        btnFdr = findViewById(R.id.btnFdr)
    }
    private fun iniSpinner(){
        val arraySpinnerModel : ArrayList<SpinnerModel> = ArrayList()
        arraySpinnerModel.add(SpinnerModel("Federal"))
        arraySpinnerModel.add(SpinnerModel("FDR"))
        // Init single select Fragment
        val spinnerSingleSelectDialogFragment =
            SpinnerDialogFragment.newInstance(
                SpinnerSelectionType.SINGLE_SELECTION,"Spinner Dialog", arraySpinnerModel,
                object :
                    OnSpinnerOKPressedListener {
                    override fun onSingleSelection(data: SpinnerModel, selectedPosition: Int) {
                        Toast.makeText(applicationContext, data.text, Toast.LENGTH_LONG).show()
                        initIntent(data.text)
                    }

                    override fun onMultiSelection(
                        data: List<SpinnerModel>,
                        selectedPosition: Int
                    ) {
                        // It will never send Multi selection data in SINGLE_SELECTION Mode
                    }

                }, 0
            )
    }
    private fun initListener(){
        btnFederal.setOnClickListener {
            iniSpinner()
        }
        btnFdr.setOnClickListener {
            initIntent("FDR")
        }
    }
    private fun initIntent(va:String){
        lateinit var intent: Intent
        if(va =="OEM"){
            intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("type", "OEM")
            startActivity(intent)
        }else if(va =="FDR") {
            intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("type", "FDR")
            startActivity(intent)
        }else if(va =="HGP") {
            intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("type", "HGP")
            startActivity(intent)
        }

    }
}