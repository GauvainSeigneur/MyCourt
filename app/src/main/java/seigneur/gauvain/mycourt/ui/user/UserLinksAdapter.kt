package seigneur.gauvain.mycourt.ui.user

import android.content.Context
import android.content.res.ColorStateList
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.utils.ListUtils

import seigneur.gauvain.mycourt.utils.Constants.BEHANCE
import seigneur.gauvain.mycourt.utils.Constants.CREATIVEMARKET
import seigneur.gauvain.mycourt.utils.Constants.FACEBOOK
import seigneur.gauvain.mycourt.utils.Constants.GITHUB
import seigneur.gauvain.mycourt.utils.Constants.INSTAGRAM
import seigneur.gauvain.mycourt.utils.Constants.LINKEDIN
import seigneur.gauvain.mycourt.utils.Constants.MEDIUM
import seigneur.gauvain.mycourt.utils.Constants.TWITTER

class UserLinksAdapter(private val mContext: Context, internal var links: Map<String, String>)
    : androidx.recyclerview.widget.RecyclerView.Adapter<LinksViewHolder>() {

    override fun getItemCount(): Int {
        return links.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinksViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_user_links, parent, false)
        return LinksViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinksViewHolder, position: Int) {
        holder.linkTitle.text = ListUtils.mapToListKey(links)[position]
        bindIcon(holder, position)
        //todo manage url on cllick
    }

    private fun bindIcon(holder: LinksViewHolder, position: Int) {
        when (ListUtils.mapToListKey(links)[position]) {
            INSTAGRAM -> {
                holder.linkIcon.setImageResource(R.drawable.ic_instagram)
                holder.linklayout.setBackgroundResource(R.drawable.social_item_background_instagram)
            }
            FACEBOOK -> {
                holder.linkIcon.setImageResource(R.drawable.ic_facebook)
                holder.linklayout.backgroundTintList = ColorStateList.valueOf(
                        mContext.resources.getColor(R.color.colorFacebook))
            }
            TWITTER -> {
                holder.linkIcon.setImageResource(R.drawable.ic_twitter)
                holder.linklayout.backgroundTintList = ColorStateList.valueOf(
                        mContext.resources.getColor(R.color.colorTwitter))
            }
            BEHANCE -> holder.linkIcon.setImageResource(R.drawable.ic_behance)
            MEDIUM -> {
                holder.linkIcon.setImageResource(R.drawable.ic_medium)
                holder.linklayout.backgroundTintList = ColorStateList.valueOf(
                        mContext.resources.getColor(R.color.colorPrimaryLight))
            }
            LINKEDIN -> holder.linkIcon.setImageResource(R.drawable.ic_linkedin)
            GITHUB -> {
                holder.linkIcon.setImageResource(R.drawable.ic_github_circle)
                holder.linklayout.backgroundTintList = ColorStateList.valueOf(
                        mContext.resources.getColor(R.color.colorGithub))
            }
            CREATIVEMARKET -> holder.linkIcon.setImageResource(R.drawable.ic_creative_market)
            else -> {
                holder.linkIcon.setImageResource(R.drawable.ic_web)
                holder.linklayout.backgroundTintList = ColorStateList.valueOf(
                        mContext.resources.getColor(R.color.colorAccent))
            }
        }
    }

}