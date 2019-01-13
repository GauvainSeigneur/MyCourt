package seigneur.gauvain.mycourt.ui.shotDraft

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.cardview.widget.CardView
import android.view.View
import android.widget.TextView

import butterknife.BindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView
import seigneur.gauvain.mycourt.utils.Constants
import java.io.File

class ShotDraftViewHolder(itemView: View, private var mCallback: ShotDraftListCallback) : BaseViewHolder(itemView),
        View.OnLongClickListener, View.OnClickListener  {
    @BindView(R.id.shot_draft_layout)
    lateinit var shotDraftLayout: androidx.cardview.widget.CardView
    @BindView(R.id.shot_draft_title)
    lateinit var shotDraftTitle: TextView
    @BindView(R.id.draft_type)
    lateinit var shotDraftType: TextView
    @BindView(R.id.shot_draft_image)
    lateinit var shotDraftImage: FourThreeImageView

    private var mDraft:Draft?=null

    fun bindTo(draft:Draft) {
        mDraft=draft
        shotDraftTitle.text = draft.shot.title
        if (draft.typeOfDraft == Constants.EDIT_MODE_NEW_SHOT) {
            shotDraftType.text = "NEW"
        } else {
            shotDraftType.text = "UPDATE"
        }

        Glide
                .with(itemView.context)
                .asDrawable()
                .load(getImageUri(draft))
                .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL) //disk Strategy to load new image and put another in Cache
                        //.override(80, 80) //dimension in Pixel
                        //.placeholder(R.mipmap.ic_launcher) //Drawables that are shown while a request is in progres
                        .error(R.drawable.dribbble_ball_intro)//Drawables that are shown wif the image is not loaded
                )
                .into(shotDraftImage)

        shotDraftLayout.setOnClickListener(this)
        shotDraftLayout.setOnLongClickListener(this)

    }

    override fun onClick(v: View?) {
        mCallback.onShotDraftClicked(mDraft!!, adapterPosition)
    }

    override fun onLongClick(v: View?): Boolean {
        mCallback.onShotDraftLongClicked(adapterPosition)
        return true
    }


    /**
     * get imageUri from Draft object. Can be an HTTP URL for published shot or Absolute URL
     * for new shot project
     * @param item - shotDraft item
     * @return uri of the image
     */
    private fun getImageUri(item: Draft): Uri? {

        if (item.imageUri != null)
            return  Uri.parse(item.imageUri)
        else
            return  Uri.parse("") //empty uri which provokes Glide Error and so dedicated drawable will be displayed
            /*if (item.typeOfDraft == Constants.EDIT_MODE_NEW_SHOT)
                FileProvider.getUriForFile(itemView.context,
                        itemView.context.getString(R.string.file_provider_authorities),
                        File(item.imageUri!!))
            else
                Uri.parse(item.imageUri)
        else
            Uri.parse("") //empty uri which provokes Glide Error and so dedicated drawable will be displayed
            */
    }

}
