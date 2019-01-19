package seigneur.gauvain.mycourt.ui.shotDetail.tags

import android.view.View

import butterknife.BindView
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder
import seigneur.gauvain.mycourt.ui.widget.TagView

class TagViewHolder(itemView: View) : BaseViewHolder(itemView) {
    @BindView(R.id.tag_item)
    lateinit var tag: TagView
}
