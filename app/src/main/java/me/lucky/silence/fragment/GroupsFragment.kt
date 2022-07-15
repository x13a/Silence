package me.lucky.silence.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import me.lucky.silence.Group
import me.lucky.silence.Preferences
import me.lucky.silence.Utils
import me.lucky.silence.databinding.FragmentGroupsBinding

class GroupsFragment : Fragment() {
    private lateinit var binding: FragmentGroupsBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            val value = prefs.groups
            local.isChecked = value.and(Group.LOCAL.value) != 0
            notLocal.isChecked = value.and(Group.NOT_LOCAL.value) != 0
            tollFree.isChecked = value.and(Group.TOLL_FREE.value) != 0
            mobile.isChecked = value.and(Group.MOBILE.value) != 0
        }
    }

    private fun setup() = binding.apply {
        local.setOnCheckedChangeListener { _, isChecked ->
            prefs.groups = Utils.setFlag(prefs.groups, Group.LOCAL.value, isChecked)
        }
        notLocal.setOnCheckedChangeListener { _, isChecked ->
            prefs.groups = Utils.setFlag(prefs.groups, Group.NOT_LOCAL.value, isChecked)
        }
        tollFree.setOnCheckedChangeListener { _, isChecked ->
            prefs.groups = Utils.setFlag(prefs.groups, Group.TOLL_FREE.value, isChecked)
        }
        mobile.setOnCheckedChangeListener { _, isChecked ->
            prefs.groups = Utils.setFlag(prefs.groups, Group.MOBILE.value, isChecked)
        }
    }
}