package seigneur.gauvain.mycourt.ui.user

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import butterknife.BindView
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder

class LinksViewHolder(itemView: View) : BaseViewHolder(itemView) {
    @BindView(R.id.user_link_layout)
    lateinit var linklayout: FrameLayout
    @BindView(R.id.link_icon)
    lateinit var linkIcon: ImageView
    @BindView(R.id.link_title)
    lateinit var linkTitle: TextView
}
