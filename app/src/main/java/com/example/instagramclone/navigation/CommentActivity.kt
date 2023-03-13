package com.example.instagramclone.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityCommentBinding
import com.example.instagramclone.navigation.model.AlarmDTO
import com.example.instagramclone.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class CommentActivity : AppCompatActivity() {

    var contentUid : String? = null
    lateinit var binding : ActivityCommentBinding
    var destinationUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCommentBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        binding.commentRecyclerview.adapter = CommentRecyclerviewAdapter()
        binding.commentRecyclerview.layoutManager = LinearLayoutManager(this)

        binding.commentBtnSend?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comnet = binding.commentEditMessage.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("coments").document().set(comment)
            commentAlarm(destinationUid!!,binding.commentEditMessage.text.toString())
            binding.commentEditMessage.setText("")
        }
    }
    fun commentAlarm(destinationUid : String, message : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener{ QuerySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if(QuerySnapshot == null)return@addSnapshotListener

                    for(snapshot in QuerySnapshot.documents!!){
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent,false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, position: Int) {
            var view = p0.itemView
            view.findViewById<TextView>(R.id.commentviewitem_textview_comment).text = comments[position].comnet
            view.findViewById<TextView>(R.id.commnentviewitem_textview_profile).text = comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(p0.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.findViewById(R.id.commentviewitem_imageview_profile))
                    }
                }
        }

    }
}