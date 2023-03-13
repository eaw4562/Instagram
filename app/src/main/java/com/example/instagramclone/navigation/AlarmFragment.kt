package com.example.instagramclone.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagramclone.R
import com.example.instagramclone.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase

class AlarmFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)
        view.findViewById<RecyclerView>(R.id.alarmfragment_recyclerview).adapter = AlarmRecyclerviewAdapter()
        view.findViewById<RecyclerView>(R.id.alarmfragment_recyclerview).layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener { QuerySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if (QuerySnapshot == null) return@addSnapshotListener

                for(snapshot in QuerySnapshot.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }
        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val url = task.result["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.findViewById(R.id.commentviewitem_imageview_profile))
                }
            }
            when(alarmDTOList[position].kind){
                0 -> {
                    val str = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    view.findViewById<TextView>(R.id.commnentviewitem_textview_profile).text = str
                }
                1 -> {
                    val str = alarmDTOList[position].userId + " " +  getString(R.string.alarm_comment) + " of " + alarmDTOList[position].message
                    view.findViewById<TextView>(R.id.commnentviewitem_textview_profile).text = str
                }
                2 -> {
                    val str = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                    view.findViewById<TextView>(R.id.commnentviewitem_textview_profile).text = str
                }

            }
            view.findViewById<TextView>(R.id.commentviewitem_textview_comment).visibility = View.INVISIBLE

        }

    }

    }
