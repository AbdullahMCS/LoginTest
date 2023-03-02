package com.example.logintest

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


const val TAG = "Main Activity TAG"
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val auth = Firebase.auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsc: GoogleSignInClient

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("325406364684-vudtgr2hqhug0qnmju2nc8tn1h3vtqp5.apps.googleusercontent.com")
            .requestEmail()
            .build()

        gsc = GoogleSignIn.getClient(this, gso)

        oneTapClient = Identity.getSignInClient(this)

        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("325406364684-vudtgr2hqhug0qnmju2nc8tn1h3vtqp5.apps.googleusercontent.com")
                    .build()
            )
            .build()

        findViewById<Button>(R.id.bt_login_with_google).setOnClickListener { signInGoogle() }
        findViewById<Button>(R.id.bt_login_with_emailpassword).setOnClickListener { signInEmailPassword() }
        findViewById<Button>(R.id.bt_login_with_sso_google).setOnClickListener { signInSSOGoogle() }
    }

    // new code
    private fun signInGoogle() {
        val signInIntent = gsc.signInIntent
        signInGoogleLauncher.launch(signInIntent)
    }

    private val signInGoogleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "firebaseAuthWithGoogle: ${account.account}")
                            auth.signOut()
                        }
                    }
                } catch (e: ApiException) {
                    Log.d(TAG, "Google sign in failed", e)
                }
            }
        }

    private fun signInSSOGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                val intent = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                signInSSOGoogleLauncher.launch(intent)
            }
            .addOnFailureListener(this) { e ->
                Log.d(TAG, e.localizedMessage)
            }
    }

    private val signInSSOGoogleLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val account = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val credential = GoogleAuthProvider.getCredential(account.googleIdToken,null)
                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Main Activity TAG", "firebaseAuthWithGoogle: ${account.id}")
                            auth.signOut()
                        }
                    }
                } catch (e: ApiException) {
                    Log.d("Main Activity TAG", "SSOGoogle sign in failed", e)
                }
            }
        }

    private fun signInEmailPassword() {
        val email = findViewById<EditText>(R.id.et_email).text.toString().trim()
        val password = findViewById<EditText>(R.id.et_password).text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "firebaseAuthWithEmailPassword: ${task.result}")
                    auth.signOut()
                } else {
                    Log.d(TAG, "EmailPassword sign in failed")
                }
            }
    }
}