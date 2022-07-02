package me.lucky.silence

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.lucky.silence.databinding.ActivityMainBinding
import me.lucky.silence.fragments.*

open class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setup()
    }

    private fun init() {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragment.id, MainFragment())
            .commit()
    }

    private fun setup() {
        binding.apply {
            appBar.setNavigationOnClickListener {
                drawer.open()
            }
            appBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.settings -> {
                        navigation.checkedItem?.isChecked = false
                        supportFragmentManager
                            .beginTransaction()
                            .replace(fragment.id, SettingsFragment())
                            .commit()
                        true
                    }
                    else -> false
                }
            }
            navigation.setNavigationItemSelectedListener {
                val frag = when (it.itemId) {
                    R.id.nav_main -> MainFragment()
                    R.id.nav_contacted -> ContactedFragment()
                    R.id.nav_groups -> GroupsFragment()
                    R.id.nav_repeated -> RepeatedFragment()
                    R.id.nav_messages -> MessagesFragment()
                    else -> MainFragment()
                }
                supportFragmentManager.beginTransaction().replace(fragment.id, frag).commit()
                it.isChecked = true
                drawer.close()
                true
            }
        }
    }
}