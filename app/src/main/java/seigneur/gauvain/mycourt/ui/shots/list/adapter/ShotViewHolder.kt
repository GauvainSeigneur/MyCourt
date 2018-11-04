package seigneur.gauvain.mycourt.ui.shots.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide

import butterknife.BindView
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder

class ShotViewHolder private constructor(itemView: View, private val mShotItemCallback: ShotItemCallback) : BaseViewHolder(itemView), View.OnClickListener {

    @BindView(R.id.shot_image)
    lateinit var shotImage: ImageView

    init {
        shotImage.setOnClickListener(this)
    }

    fun bindTo(shot: Shot) {
        //shotname.setText(shot.getTitle());
        Glide.with(itemView.context)
                .load(shot.imageUrl)
                //.placeholder(R.mipmap.ic_launcher)
                .into(shotImage)
        //gifIcon.setVisibility(user.isSiteAdmin() ? View.VISIBLE : View.GONE);
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.shot_image -> mShotItemCallback.onShotClicked(adapterPosition)
        }
    }

    companion object {

        fun create(parent: ViewGroup, shotItemCallback: ShotItemCallback): ShotViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_shot, parent, false)
            return ShotViewHolder(view, shotItemCallback)
        }
    }

}
