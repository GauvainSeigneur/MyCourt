package seigneur.gauvain.mycourt.ui.shotEdition.attachmentList

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide

import butterknife.BindView
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder

class AddAttachmentViewHolder private constructor(itemView: View,
                                                  private val attachmentItemCallback: AttachmentItemCallback) :
        BaseViewHolder(itemView), View.OnClickListener {

    @BindView(R.id.add)
    lateinit var mAdd: ImageView

    init {
        mAdd.setOnClickListener(this)
    }

    fun bindTo(isVisible:Boolean) {
        //todo
    }

    fun seVisible(isVisible:Boolean) {
        if(isVisible)
            itemView.visibility=View.VISIBLE
        else
            itemView.visibility=View.GONE
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.add -> attachmentItemCallback.onAddClicked()
        }
    }

    companion object {
        fun create(parent: ViewGroup, attachmentItemCallback: AttachmentItemCallback): AddAttachmentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_add, parent, false)
            return AddAttachmentViewHolder(view, attachmentItemCallback)
        }
    }

}
