package me.lucky.silence.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.lucky.silence.Message
import me.lucky.silence.Preferences
import me.lucky.silence.Utils
import me.lucky.silence.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {
    private lateinit var binding: FragmentMessagesBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences

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
        }
    }

    private fun setup() {
        binding.apply {
            inbox.setOnCheckedChangeListener { _, isChecked ->
                prefs.messages = Utils.setFlag(prefs.messages, Message.INBOX.value, isChecked)
            }
            text.setOnCheckedChangeListener { _, isChecked ->
                prefs.messages = Utils.setFlag(prefs.messages, Message.TEXT.value, isChecked)
            }
        }
    }
}