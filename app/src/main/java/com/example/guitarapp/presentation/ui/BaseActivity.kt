package com.example.guitarapp.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.guitarapp.R
import com.example.guitarapp.databinding.ActivityBaseBinding
import com.example.guitarapp.presentation.ui.profile.ProfileFragment
import com.example.guitarapp.presentation.ui.tutorial.TutorialFragment

class BaseActivity : AppCompatActivity(){

    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarClicks()
    }

    private fun setupToolbarClicks() {

        binding.bottomToolbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ivCoverImg -> {
                    replaceFragment(TutorialFragment())
                }
                R.id.ivCreateCoverImg -> {

                }
                R.id.ivInstrumentImg -> {
                }
                R.id.ivProfileImg -> {
                    replaceFragment(ProfileFragment())
                }
                else -> {
                    false
                }
            }

            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content_container, fragment)
        fragmentTransaction.commit()
    }
}