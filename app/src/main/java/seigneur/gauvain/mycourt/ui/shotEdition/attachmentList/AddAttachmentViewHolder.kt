package seigneur.gauvain.mycourt.ui.shotEdition.attachmentList


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import butterknife.BindView
import com.google.android.material.button.MaterialButton
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.base.BaseViewHolder

class AddAttachmentViewHolder private constructor(itemView: View,
                                                  private val attachmentItemCallback: AttachmentItemCallback) :
        BaseViewHolder(itemView), View.OnClickListener {

    @BindView(R.id.add)
    lateinit var mAdd: MaterialButton

    init {
        mAdd.setOnClickListener(this)
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
