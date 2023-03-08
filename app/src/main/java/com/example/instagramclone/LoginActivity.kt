package com.example.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.instagramclone.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.emailLoginButton.setOnClickListener {
            signinAndSignup()
        }
    }
    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(binding.emailEdittext.text.toString().trim(), binding.passwordEdittext.text.toString().trim())?.addOnCompleteListener {
            task ->
                if(task.isSuccessful){
                    Toast.makeText(this,"로그인성공",Toast.LENGTH_SHORT).show()
                    //아이디 생성 성공
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
        fun moveMainPage(user:FirebaseUser?){
            if(user != null){
                startActivity(Intent(this,MainActivity::class.java))
            }
        }
    }
}