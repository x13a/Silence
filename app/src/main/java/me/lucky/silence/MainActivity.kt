package me.lucky.silence

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import me.lucky.silence.databinding.ActivityMainBinding
import me.lucky.silence.fragment.*

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
        NotificationManager(this).createNotificationChannels()
        replaceFragment(MainFragment())
    }

    private fun setup() = binding.apply {
        appBar.setNavigationOnClickListener {
            drawer.open()
        }
        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.top_settings -> {
                    replaceFragment(when (supportFragmentManager.fragments.last()) {
                        is SettingsFragment ->
                            getFragment(navigation.checkedItem?.itemId ?: R.id.nav_main)
                        else -> SettingsFragment()
                    })
                    true
                }
                else -> false
            }
        }
        navigation.setNavigationItemSelectedListener {
            replaceFragment(getFragment(it.itemId))
            it.isChecked = true
            drawer.close()
            true
        }
    }

    private fun replaceFragment(f: Fragment) =
        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragment.id, f)
            .commit()

    private fun getFragment(id: Int) = when (id) {
        R.id.nav_main -> MainFragment()
        R.id.nav_contacted -> ContactedFragment()
        R.id.nav_groups -> GroupsFragment()
        R.id.nav_repeated -> RepeatedFragment()
        R.id.nav_messages -> MessagesFragment()
        else -> MainFragment()
    }
}