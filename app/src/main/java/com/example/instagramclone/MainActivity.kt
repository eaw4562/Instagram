package com.example.instagramclone

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.instagramclone.databinding.ActivityMainBinding
import com.example.instagramclone.navigation.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class MainActivity : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding : ActivityMainBinding
    lateinit var auth : FirebaseAuth
    lateinit var message : FirebaseMessaging
    lateinit var firestore : FirebaseFirestore

    override fun onNavigationItemSelected(parent: MenuItem): Boolean {
        when(parent.itemId){
            R.id.action_home->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.action_search->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }
            R.id.action_add_photo-> {

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                }
                return true
            }

            R.id.action_favorite_alarm-> {
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, alarmFragment)
                    .commit()
                return true
            }

            R.id.action_account->{
                var userFragment = UserFragment()
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid

                bundle.putString("uid",auth.uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
                return true
            }

        }
        return false
    }
        fun setToolbarDefault(){
            findViewById<TextView>(R.id.toolbar_username).visibility = View.GONE
            findViewById<ImageView>(R.id.toolbar_btn_back).visibility = View.GONE
            findViewById<ImageView>(R.id.toolbar_title_image).visibility = View.VISIBLE
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener (this)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

        //set default screen
        binding.bottomNavigation.selectedItemId = R.id.action_home
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == RESULT_OK){
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWith { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWith storageRef.downloadUrl }
                .addOnSuccessListener { uri ->
                    var map = HashMap<String,Any>()
                    map["image"] = uri.toString()
                    FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
                }
        }
    }


    fun saveMyPushToken(){
        message.token.addOnCompleteListener { task ->
            if(task.isSuccessful){

                var token = task.result
                var map = mutableMapOf<String,Any>()
                map["token"] = token

                firestore.collection("pushtokens").document(auth?.uid!!).set(map)

            }

        }
    }

}