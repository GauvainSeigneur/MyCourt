package seigneur.gauvain.mycourt.ui.shotDetail.attachments

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import seigneur.gauvain.mycourt.data.model.Attachment
import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.shot_detail_attachements.view.*
import seigneur.gauvain.mycourt.R
import timber.log.Timber

class AttachmentGridAdapter(context: Context, attachments: List<Attachment>)  :BaseAdapter() {

    private val mAttachment=attachments
    private val mContext=context

    init { }

    override fun getCount(): Int {
        return mAttachment.size
    }

    override fun getItem(position: Int): Any {
        return mAttachment[position]
    }

    override fun getItemId(position: Int): Long {
        return mAttachment[position].id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView: View
        //gridView = View(mContext)
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            itemView = inflater.inflate(R.layout.shot_detail_attachements, null) as View
            //itemView.layoutParams = ViewGroup.LayoutParams(1000, 1000)
            // set image based on selected text
            val imageView = itemView.findViewById(R.id.attachment_image) as ImageView
           // imageView.layoutParams = ImageView.LayoutParams(1000, 1000)
        } else {
            itemView = convertView
        }

        Timber.d("octogone: "+mAttachment[position].uri)
        Glide.with(itemView.context)
                .asDrawable()
                .load(mAttachment[position].uri)
                .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) //disk Strategy to load new image and put another in Cache
                        .error(R.drawable.dribbble_ball_intro)//Drawables that are shown wif the image is not loaded
                        .centerCrop()
                )
                .into(itemView.attachment_image)
        return itemView
    }

}