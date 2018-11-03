package seigneur.gauvain.mycourt.ui.shotDraft

import android.support.v7.widget.CardView
import android.view.View
import android.widget.TextView

import butterknife.BindView
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView

class ShotDraftViewHolder(itemView: View) : BaseViewHolder(itemView) {
    @BindView(R.id.shot_draft_layout)
    lateinit var shotDraftLayout: CardView
    @BindView(R.id.shot_draft_title)
    lateinit var shotDraftTitle: TextView
    @BindView(R.id.draft_type)
    lateinit var shotDraftType: TextView
    @BindView(R.id.shot_draft_image)
    lateinit var shotDraftImage: FourThreeImageView
}
