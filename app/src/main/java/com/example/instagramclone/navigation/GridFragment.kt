package com.example.instagramclone.navigation

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagramclone.R
import com.example.instagramclone.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class GridFragment : Fragment() {

    var firestore : FirebaseFirestore? = null
    var fragmentView : View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_grid, container, false)
        fragmentView?.findViewById<RecyclerView>(R.id.gridfragment_recyclerview)?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.findViewById<RecyclerView>(R.id.gridfragment_recyclerview)?.layoutManager = GridLayoutManager(activity,3)
        return fragmentView
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init{
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null) {
                    Log.e(ContentValues.TAG, "Listen failed!", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    contentDTOs.clear()

                    for (snapshot in querySnapshot.documents) {
                        val item = snapshot.toObject(ContentDTO::class.java)
                        if (item != null) {
                            contentDTOs.add(item)
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3
            val imageView = ImageView(parent.context).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            }
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            // CustomViewHolder 클래스 내부에서 imageView에 접근하려면 아래와 같이 바인딩할 수 있습니다.
            val imageView: ImageView = itemView as ImageView
        }


        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, position: Int) {
            var imageView = (parent as CustomViewHolder).imageView
            Glide.with(parent.imageView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(imageView)
        }

    }

    }
