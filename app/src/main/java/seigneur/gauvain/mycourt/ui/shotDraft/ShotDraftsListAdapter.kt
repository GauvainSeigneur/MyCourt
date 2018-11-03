package seigneur.gauvain.mycourt.ui.shotDraft

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

import java.io.File
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.utils.Constants

/**
 * ShotDraft RecyclerView adapter
 */
class ShotDraftsListAdapter
/**
 * Constructor
 * @param context   - activity
 * @param data      - list of ShotDraft object
 * @param callback  - ShotDraftListCallback implementation
 */
(private val context: Context, private val data: MutableList<Draft>, var mCallback: ShotDraftListCallback) : RecyclerView.Adapter<ShotDraftViewHolder>() {
    //var shotDraftViewHolder: ShotDraftViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShotDraftViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_post, parent, false)
        return ShotDraftViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShotDraftViewHolder, position: Int) {
        val item = data[position]
        holder.shotDraftTitle.text = item.shot.title
        if (item.typeOfDraft == Constants.EDIT_MODE_NEW_SHOT) {
            holder.shotDraftType.text = "NEW"
        } else {
            holder.shotDraftType.text = "UPDATE"
        }
        if (data[position].imageUri != null) {
            Glide
                    .with(context)
                    .asDrawable()
                    .load(getImageUri(item)/*new File(item.getImageUrl())*/)
                    .apply(RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE) //disk Strategy to load new image and put another in Cache
                            //.override(80, 80) //dimension in Pixel
                            //.placeholder(R.mipmap.ic_launcher) //Drawables that are shown while a request is in progres
                            .error(R.mipmap.ic_launcher)//Drawables that are shown wif the image is not loaded
                    )
                    .into(holder.shotDraftImage)
        } else {
            holder.shotDraftImage.setImageResource(R.drawable.dribbble_ball_intro)
        }
        holder.shotDraftLayout.setOnClickListener { _ -> mCallback.onShotDraftClicked(data[position], position) }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun clear() {
        while (itemCount > 0) {
            data.clear()
        }
    }

    /**
     * get imageUri from ShotDraft object. Can be an HTTP URL for published shot or Absolute URL
     * for new shot project
     * @param item - shotDraft item
     * @return uri of the image
     */
    private fun getImageUri(item: Draft): Uri? {
        return if (item.imageUri != null)
            if (item.typeOfDraft == Constants.EDIT_MODE_NEW_SHOT)
                FileProvider.getUriForFile(context,
                        context.getString(R.string.file_provider_authorities),
                        File(item.imageUri!!))
            else
                Uri.parse(item.imageUri)
        else
            null
    }

}
