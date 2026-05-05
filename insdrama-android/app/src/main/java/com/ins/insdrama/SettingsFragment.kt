package com.ins.insdrama

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment() {

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireActivity().getSharedPreferences("insdrama_prefs", 0)

        setupOrientation(view)
        setupAutoPlay(view)
        setupRawUrl(view)
        setupClearCache(view)
        setupVersion(view)
    }

    private fun setupOrientation(view: View) {
        val radioGroup = view.findViewById<RadioGroup>(R.id.orientationRadioGroup)
        val autoRadio = view.findViewById<RadioButton>(R.id.autoOrientation)
        val portraitRadio = view.findViewById<RadioButton>(R.id.portraitOrientation)
        val landscapeRadio = view.findViewById<RadioButton>(R.id.landscapeOrientation)

        val savedOrientation = prefs.getString("orientation", "auto")
        when (savedOrientation) {
            "auto" -> autoRadio.isChecked = true
            "portrait" -> portraitRadio.isChecked = true
            "landscape" -> landscapeRadio.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val orientation = when (checkedId) {
                R.id.autoOrientation -> "auto"
                R.id.portraitOrientation -> "portrait"
                R.id.landscapeOrientation -> "landscape"
                else -> "auto"
            }
            prefs.edit().putString("orientation", orientation).apply()
        }
    }

    private fun setupAutoPlay(view: View) {
        val autoPlaySwitch = view.findViewById<SwitchMaterial>(R.id.autoPlaySwitch)
        autoPlaySwitch.isChecked = prefs.getBoolean("auto_play", true)

        autoPlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_play", isChecked).apply()
        }
    }

    private fun setupRawUrl(view: View) {
        val rawUrlInput = view.findViewById<TextInputEditText>(R.id.rawUrlInput)
        val saveUrlButton = view.findViewById<MaterialButton>(R.id.saveUrlButton)

        rawUrlInput.setText(prefs.getString("raw_url", "https://raw.githubusercontent.com/INsITdeveloper/Drama-extension/main/datadrama.json"))

        saveUrlButton.setOnClickListener {
            val url = rawUrlInput.text.toString()
            if (url.isNotBlank()) {
                prefs.edit().putString("raw_url", url).apply()
                // Show success message
            }
        }
    }

    private fun setupClearCache(view: View) {
        val clearCacheButton = view.findViewById<MaterialButton>(R.id.clearCacheButton)
        clearCacheButton.setOnClickListener {
            // Clear cache logic
            prefs.edit().clear().apply()
        }
    }

    private fun setupVersion(view: View) {
        val versionText = view.findViewById<TextView>(R.id.versionText)
        versionText.text = "InsDrama v1.0.0"
    }
}
