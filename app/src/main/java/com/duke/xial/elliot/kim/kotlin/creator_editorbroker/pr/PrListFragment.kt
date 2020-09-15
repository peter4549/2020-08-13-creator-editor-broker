package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.pr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.BaseRecyclerViewAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.SpinnerAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_PR_LIST
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.UserType.CREATOR
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.VERTICAL
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PrModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.toLocalTimeString
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_pr_list.view.*
import kotlinx.android.synthetic.main.item_view_pr.view.*
import org.json.JSONObject

class PrListFragment : Fragment() {

    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var prListRecyclerViewAdapter: PrListRecyclerViewAdapter
    @Suppress("SpellCheckingInspection")
    private val gson = GsonBuilder().create()
    private val targets: Array<String?> = arrayOfNulls(2)
    private var selectedTarget = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pr_list, container, false)
        targets[0] = getString(R.string.pr_list_find_creator)
        targets[1] = getString(R.string.pr_list_find_editor)
        initializeSpinners(view.spinner_target, targets.filterNotNull().toTypedArray())
        prListRecyclerViewAdapter = PrListRecyclerViewAdapter(R.layout.item_view_pr)
        initRecyclerView(view.recycler_view_pr_list)
        return view
    }



    private fun initializeSpinners(spinner: Spinner, array: Array<String>) {
        spinner.adapter = SpinnerAdapter(requireContext(), array)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedTarget = position
                prListRecyclerViewAdapter.registerPrListSnapshotListener()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {  }
        }
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = prListRecyclerViewAdapter
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
        }
    }

    inner class PrListRecyclerViewAdapter(layoutId: Int)
        : BaseRecyclerViewAdapter<PrModel>(layoutId) {

        private val collectionReference = FirebaseFirestore.getInstance()
            .collection(COLLECTION_PR_LIST)

        fun registerPrListSnapshotListener() {
            if (::listenerRegistration.isInitialized)
                listenerRegistration.remove()

            items.clear()
            notifyDataSetChanged()

            listenerRegistration = collectionReference
                .whereEqualTo(PrModel.KEY_TARGET, selectedTarget + 1)
                .addSnapshotListener { documentSnapshot, fireStoreException ->
                if (fireStoreException != null)
                    println("$TAG: $fireStoreException")
                else {
                    for (change in documentSnapshot!!.documentChanges) {
                        showToast(requireContext(), "I AM PR LISTIN THE HOLE")
                        when (change.type) {
                            DocumentChange.Type.ADDED -> this.insert(mapToPrModel(change.document.data))
                            DocumentChange.Type.MODIFIED -> this.findMessageAndUpdate(mapToPrModel(change.document.data))
                            DocumentChange.Type.REMOVED -> this.remove(mapToPrModel(change.document.data))
                            else -> { println("$TAG: unexpected DocumentChange Type") }
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }

        private fun findMessageAndUpdate(prModel: PrModel) {
            val item = items.find { it.id == prModel.id }
            val position = items.indexOf(item)

            if (position == -1)
                return

            items[position] = prModel
            notifyItemChanged(position)
        }

        private fun mapToPrModel(map: Map<*, *>) =
            gson.fromJson(JSONObject(map).toString(), PrModel::class.java)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pr = items[position]
            val thumbnailUri = pr.youtubeVideos[0].let {
                it?.thumbnailUri ?: it
            } as String?
            loadImage(holder.view.image_view_thumbnail, thumbnailUri)

            val targetText = when(pr.target) {
                CREATOR -> getString(R.string.find_creator)
                else -> getString(R.string.find_editor)
            }

            val comments = pr.comments.filter { it.uid != pr.publisherId }

            holder.view.text_view_title.text = pr.title
            // holder.view.text_view_public_name.text = pr.publisherPublicName
            holder.view.text_view_target.text = targetText
            holder.view.text_view_published_time.text = pr.registrationTime.toLocalTimeString()
            holder.view.text_view_categories.text = pr.categories.mapNotNull { MainActivity.categoriesMap[it] }.joinToString()
            holder.view.text_view_comments.text = comments.count().toString()
            holder.view.text_view_favorites.text = pr.favoriteUserIds.count().toString()
            holder.view.text_view_stars.text = pr.stars.toString()

            holder.view.setOnClickListener {
                (requireActivity() as MainActivity).startFragment(
                    PrFragment(pr),
                    R.id.frame_layout_activity_main,
                    MainActivity.TAG_PR_FRAGMENT,
                    VERTICAL)
            }
        }

        private fun loadImage(imageView: ImageView, uri: String?) {
            if (uri == null)
                imageView.visibility = View.GONE
            else {
                Glide.with(imageView.context)
                    .load(uri)
                    .error(R.drawable.ic_baseline_sentiment_dissatisfied_80)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .transform(CenterCrop(), RoundedCorners(8))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(null)
                    .into(imageView)
            }
        }
    }

    fun removePrSnapshotListener() {
        if (::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    companion object {
        const val TAG = "PrListFragment"
    }

}