package me.lucky.silence.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import me.lucky.silence.Contact
import me.lucky.silence.Preferences
import me.lucky.silence.Utils
import me.lucky.silence.databinding.FragmentContactedBinding

class ContactedFragment : Fragment() {
    private lateinit var binding: FragmentContactedBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactedBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            val value = prefs.contacted
            call.isChecked = value.and(Contact.CALL.value) != 0
            message.isChecked = value.and(Contact.MESSAGE.value) != 0
        }
    }

    private fun setup() = binding.apply {
        call.setOnCheckedChangeListener { _, isChecked ->
            prefs.contacted = Utils.setFlag(prefs.contacted, Contact.CALL.value, isChecked)
        }
        message.setOnCheckedChangeListener { _, isChecked ->
            prefs.contacted = Utils.setFlag(prefs.contacted, Contact.MESSAGE.value, isChecked)
        }
    }
}