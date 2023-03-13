package com.example.instagramclone.navigation

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentDetailBinding
import com.example.instagramclone.navigation.model.AlarmDTO
import com.example.instagramclone.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var binding: FragmentDetailBinding? = null
    var uid = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        view?.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview)?.adapter = DetailViewRecyclerViewAdapter()
        view?.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview)?.layoutManager = LinearLayoutManager(activity)
        uid = FirebaseAuth.getInstance().currentUser?.uid
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.detailviewfragmentRecyclerview?.adapter = DetailViewRecyclerViewAdapter()
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { QuerySnapshot, FirebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                if (QuerySnapshot == null) return@addSnapshotListener

                for (snapshot in QuerySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView

            //UserId
            viewHolder.findViewById<TextView>(R.id.detailviewitem_profile_textview).text = contentDTOs[position].userId

            //Image
            Glide.with(viewHolder).load(contentDTOs[position].imageUrl).into(viewHolder.findViewById(R.id.detailviewitem_imageview_content))

            //Explain of content
            viewHolder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text = contentDTOs[position].explain

            //likes
            viewHolder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview).text = "Likes" + contentDTOs[position].favoriteCount

            //ProfileImage
            //Glide.with(viewHolder).load(contentDTOs[position].imageUrl).into(viewHolder.findViewById(R.id.detailviewitem_imageview_content))

            viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setOnClickListener {
                favoriteEvent(position)
            }

            if(contentDTOs[position].favorites.containsKey(uid)){
                //좋아요 버튼 클릭
                viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setImageResource(R.drawable.ic_favorite)
            }else{
                //좋아요 버튼 아직 클린 안함
                viewHolder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setImageResource(R.drawable.ic_favorite_border)
            }

            //
            viewHolder.findViewById<ImageView>(R.id.detailviewitem_profile_image).setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("userId",contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
            viewHolder.findViewById<ImageView>(R.id.detailviewitem_comment_imageview).setOnClickListener {v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid",contentDTOs[position].uid)
                startActivity(intent)
            }
        }

        private fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    //버튼 클릭
                    contentDTO.favoriteCount -= 1
                    contentDTO.favorites.remove(uid)

                } else {
                    //버튼 클릭 안됨
                    contentDTO.favoriteCount += 1
                    contentDTO.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)

                }

                transaction.set(tsDoc, contentDTO)
            }
        }
        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}


