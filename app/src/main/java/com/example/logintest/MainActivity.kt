package com.example.logintest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("325406364684-vudtgr2hqhug0qnmju2nc8tn1h3vtqp5.apps.googleusercontent.com")
            .requestEmail()
            .build()

        gsc = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.login_with_google).setOnClickListener { signIn() }

    }

    private fun signIn() {
        val signInIntent = gsc.signInIntent
        startActivityForResult(signInIntent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("Main Activity", "firebaseAuthWithGoogle:" + account.id)
            } catch (e: ApiException) {
                Log.w("Main Activity", "Google sign in failed", e)
            }
        }
    }
}