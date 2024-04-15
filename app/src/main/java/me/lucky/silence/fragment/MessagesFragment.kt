package me.lucky.silence.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import me.lucky.silence.Message
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.Utils
import me.lucky.silence.databinding.FragmentMessagesBinding
import java.util.regex.Pattern

class MessagesFragment : Fragment() {
    companion object {
        private const val MODIFIER_DAYS = 'd'
        private const val MODIFIER_HOURS = 'h'
        private const val MODIFIER_MINUTES = 'm'
    }

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences
    private val timePattern by lazy {
        Pattern.compile("^[1-9]\\d*[$MODIFIER_DAYS$MODIFIER_HOURS$MODIFIER_MINUTES]$") }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            val value = prefs.messages
            inbox.isChecked = value.and(Message.INBOX.value) != 0
            text.isChecked = value.and(Message.TEXT.value) != 0
            val ttl = prefs.messagesTextTtl
            time.editText?.setText(when {
                ttl % (24 * 60) == 0 -> "${ttl / 24 / 60}$MODIFIER_DAYS"
                ttl % 60 == 0 -> "${ttl / 60}$MODIFIER_HOURS"
                else -> "$ttl$MODIFIER_MINUTES"
            })
        }
    }

    private fun setup() = binding.apply {
        inbox.setOnCheckedChangeListener { _, isChecked ->
            prefs.messages = Utils.setFlag(prefs.messages, Message.INBOX.value, isChecked)
        }
        text.setOnCheckedChangeListener { _, isChecked ->
            prefs.messages = Utils.setFlag(prefs.messages, Message.TEXT.value, isChecked)
        }
        time.editText?.doAfterTextChanged {
            val str = it?.toString() ?: ""
            if (!timePattern.matcher(str).matches()) {
                time.error = ctx.getString(R.string.messages_text_ttl_error)
                return@doAfterTextChanged
            }
            if (str.length < 2) return@doAfterTextChanged
            val modifier = str.last()
            val i = str.dropLast(1).toIntOrNull() ?: return@doAfterTextChanged
            prefs.messagesTextTtl = when (modifier) {
                MODIFIER_DAYS -> i * 24 * 60
                MODIFIER_HOURS -> i * 60
                MODIFIER_MINUTES -> i
                else -> return@doAfterTextChanged
            }
            time.error = null
        }
        gotoButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }
}