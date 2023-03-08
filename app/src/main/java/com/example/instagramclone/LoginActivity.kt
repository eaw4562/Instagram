package com.example.instagramclone

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.instagramclone.databinding.ActivityLoginBinding
import com.example.instagramclone.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    var auth : FirebaseAuth? = null
    var googleSignClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.emailLoginButton.setOnClickListener {
            signinAndSignup()
        }
        binding.googleSignInButton.setOnClickListener {
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("491777352904-d2fob2ohg00vjshbb5h7iljrlf54ph9a.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignClient = GoogleSignIn.getClient(this,gso)
    }
    fun googleLogin(){
        var signIntent = googleSignClient!!.signInIntent
        startForResult.launch(signIntent)
    }


    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->

            if(result.resultCode == RESULT_OK){
                val intent: Intent = result.data!!
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(ContentValues.TAG,"firebaseAuthWithGoogle" + account.id)
                    firebaseAuthWithGoogle(account)
                }catch (e: ApiException){
                    Log.w(ContentValues.TAG, "Google sign in failed",e)
                }
            }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMainPage(task.result?.user)
            }else{
                Toast.makeText(this,"Authentication failed", Toast.LENGTH_SHORT).show()
            }
            }
        }

    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(binding.emailEdittext.text.toString().trim(), binding.passwordEdittext.text.toString().trim())?.addOnCompleteListener {
            task ->
                if(task.isSuccessful){
                    Toast.makeText(this,"로그인성공",Toast.LENGTH_SHORT).show()
                    //아이디 생성 성공
                    moveMainPage(task.result?.user)
                }else if(task.exception?.message.isNullOrEmpty()){
                    //로그인 실패
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }else{
                    siginEmail()
                }
        }
    }
    fun siginEmail(){
        auth?.signInWithEmailAndPassword(binding.emailEdittext.text.toString().trim(), binding.passwordEdittext.text.toString().trim())?.addOnCompleteListener {
                task ->
                     if (task.isSuccessful) {
                //로그인 성공

            } else {

            }
        }
    }
    fun moveMainPage(user:FirebaseUser?){
        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }


}