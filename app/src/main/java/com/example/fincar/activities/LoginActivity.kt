package com.example.fincar.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.fincar.R
import com.example.fincar.activities.main.MainActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        handler = Handler()
        runnable = Runnable {
            if(FirebaseAuth.getInstance().currentUser == null){
                showSignInOption()
            }else startMainActivity()
        }

        initProviders()

    }

    private fun initProviders() {
        providers = listOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )
    }

    override fun onResume() {
        super.onResume()
        splashIcon.animation = AnimationUtils
            .loadAnimation(applicationContext,
                R.anim.slide_in_left
            )

        handler.postDelayed(runnable, 1000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showSignInOption() {
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(true) //set true to save credentials
                .setTheme(R.style.AuthUiTheme)
                .build(),
            LOGIN_REQUEST
        )
    }

    companion object {
        private const val LOGIN_REQUEST = 22
    }
}
