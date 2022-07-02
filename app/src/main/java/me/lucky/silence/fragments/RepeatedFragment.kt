package me.lucky.silence.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import me.lucky.silence.Preferences
import me.lucky.silence.databinding.FragmentRepeatedBinding

class RepeatedFragment : Fragment() {
    private lateinit var binding: FragmentRepeatedBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRepeatedBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            count.editText?.setText(prefs.repeatedCount.toString())
            minutes.editText?.setText(prefs.repeatedMinutes.toString())
        }
    }

    private fun setup() {
        binding.apply {
            count.editText?.doAfterTextChanged {
                try {
                    prefs.repeatedCount = it?.toString()?.toInt() ?: return@doAfterTextChanged
                } catch (exc: NumberFormatException) {}
            }
            minutes.editText?.doAfterTextChanged {
                try {
                    prefs.repeatedMinutes = it?.toString()?.toInt() ?: return@doAfterTextChanged
                } catch (exc: NumberFormatException) {}
            }
        }
    }
}