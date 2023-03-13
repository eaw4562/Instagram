package com.example.instagramclone.navigation

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagramclone.LoginActivity
import com.example.instagramclone.MainActivity
import com.example.instagramclone.R
import com.example.instagramclone.navigation.model.AlarmDTO
import com.example.instagramclone.navigation.model.ContentDTO
import com.example.instagramclone.navigation.model.FollowDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment : Fragment() {
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null
    companion object{
      var  PICK_PROFILE_FROM_ALBUM = 10
    }

    private lateinit var adapter : UserFragmentRecyclerViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_userl, container, false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        adapter = UserFragmentRecyclerViewAdapter()
        currentUserUid = auth?.currentUser?.uid

        if (uid == currentUserUid){
            //Mypage
            fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.text = getString(R.string.signout)
            fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }
        }else{
            //다른사람 유저 페이지
            fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.text = getString(R.string.follow)
            var mainActivity = (activity as MainActivity)
            mainActivity?.findViewById<TextView>(R.id.toolbar_username)?.text = arguments?.getString("userId")
            mainActivity?.findViewById<Button>(R.id.toolbar_btn_back)?.setOnClickListener {
                mainActivity.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.action_home
            }
            mainActivity.findViewById<ImageView>(R.id.toolbar_title_image).visibility = View.GONE
            mainActivity.findViewById<ImageView>(R.id.toolbar_username).visibility = View.VISIBLE
            mainActivity.findViewById<ImageView>(R.id.toolbar_btn_back).visibility = View.VISIBLE
            fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.setOnClickListener {
                requestFollow()
            }

        }

        fragmentView?.findViewById<RecyclerView>(R.id.account_recyclerview)?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.findViewById<RecyclerView>(R.id.account_recyclerview)?.layoutManager = GridLayoutManager(requireContext(), 3)

        fragmentView?.findViewById<ImageView>(R.id.account_iv_profile)?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getprofileImage()
        return fragmentView
    }
    fun getFollowerAndFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                fragmentView?.findViewById<TextView>(R.id.account_tv_following_count)?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null){
                fragmentView?.findViewById<TextView>(R.id.account_tv_folloewr_count)?.text = followDTO?.followerCount?.toString()
                if(followDTO?.followers?.containsKey(currentUserUid)!!){
                    fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.text = getString(R.string.follow_cancel)
                    fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.background?.setColorFilter(ContextCompat.getColor(requireActivity(),R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
                }else{
                    fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.text = getString(R.string.follow)
                    if(uid != currentUserUid){
                        fragmentView?.findViewById<Button>(R.id.account_btn_follow_signout)?.background?.colorFilter = null
                    }

                }
            }
        }
    }
    fun requestFollow() {
        //나의 계정에 상대방 팔로우 목록
        var tsDocFollwing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollwing!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followers[uid!!] = true

                transaction.set(tsDocFollwing, followDTO)
                return@runTransaction
            }
            if (followDTO.followings.containsKey(uid)) {
                //팔로우 삭제
                followDTO?.followingCount = followDTO?.followingCount!! - 1
                followDTO?.followers?.remove(uid)

            } else {
                followDTO?.followingCount = followDTO?.followingCount!! + 1
                followDTO?.followers?.set(uid!!, true)
            }
            transaction.set(tsDocFollwing, followDTO)
            return@runTransaction
        }
        //내가 팔로잉한 상대방 계정 접근
        val tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction{transition ->
            var followDTO = transition.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)

                transition.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }
            if(followDTO!!.followers.containsKey(currentUserUid)){
                //내가 상대방 계정에 팔로우 햇을 경우
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)
            }else{
                //내가 상대방 계정에 팔로우 하지 않앗을 경우
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transition.set(tsDocFollower, followDTO!!)
            return@runTransaction

        }

    }
    fun followerAlarm(destinationUid: String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }

    private fun showMessage(activity: FragmentActivity?, message: String) {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    fun getprofileImage() {
        firestore?.collection("proflieImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot == null) return@addSnapshotListener
            if (documentSnapshot.data != null) {
                var url = documentSnapshot?.data!!["image"]
                val fragmentActivity = activity as? FragmentActivity ?: return@addSnapshotListener
                Glide.with(fragmentActivity)
                    .load(url)
                    .apply(RequestOptions().circleCrop())
                    .into(fragmentView?.findViewById(R.id.account_iv_profile)!!)
            }
        }
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init{
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "Listen failed!", firebaseFirestoreException)
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
                    fragmentView?.findViewById<TextView>(R.id.account_tv_post_count)?.text = contentDTOs.size.toString()
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
            Glide.with(parent.imageView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

    }

    }
