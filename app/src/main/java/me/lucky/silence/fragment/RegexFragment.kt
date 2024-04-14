package me.lucky.silence.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.databinding.FragmentRegexBinding

class RegexFragment : Fragment() {
    private lateinit var binding: FragmentRegexBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegexBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            pattern.editText?.setText(prefs.regexPattern)
        }
    }

    private fun setup() = binding.apply {
        pattern.editText?.doAfterTextChanged {
            val s = it?.toString().orEmpty()
            if (isValidRegex(s)) {
                prefs.regexPattern = s
                pattern.error = null
            } else { pattern.error = ctx.getString(R.string.regex_pattern_error) }
            return@doAfterTextChanged
        }
    }

    private fun isValidRegex(pattern: String): Boolean {
        try { pattern.toRegex(RegexOption.MULTILINE) }
        catch (exc: java.util.regex.PatternSyntaxException) { return false }
        return true
    }
}