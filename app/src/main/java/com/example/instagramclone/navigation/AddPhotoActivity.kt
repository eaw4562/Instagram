package com.example.instagramclone.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityAddPhotoBinding
import com.example.instagramclone.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    lateinit var binding : ActivityAddPhotoBinding
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Initiate stoarge
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //open album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //add image upload event
        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                //사진 선택시 경로
                photoUri = data?.data
                binding.addphotoImage.setImageURI(photoUri)
            }else{
                //취소 버튼 눌럿을때 경로
                finish()
            }
        }
    }
    fun contentUpload(){

        var timestamp = SimpleDateFormat("yyyyMMDD_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //1.File 업로드방식 Promise method
        storageRef?.putFile(photoUri!!)?.continueWith{ task: Task<UploadTask.TaskSnapshot> ->
            return@continueWith storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //Insert downloadUrl of image
            contentDTO.imageUrl = uri.toString()

            //Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userId = auth?.currentUser?.email

            //Insert explain of content
            contentDTO.explain = binding.addphotoImageEditExplain.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }

        //2.File 업로드 콜백 메소드
       /* storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                //Insert downloadUrl of image
                contentDTO.imageUrl = uri.toString()

                //Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert userId
                contentDTO.userId = auth?.currentUser?.email

                //Insert explain of content
                contentDTO.explain = binding.addphotoImageEditExplain.text.toString()

                //Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }*/
    }
}