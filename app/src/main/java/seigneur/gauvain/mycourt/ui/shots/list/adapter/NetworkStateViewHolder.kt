package seigneur.gauvain.mycourt.ui.shots.list.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView


import butterknife.BindView
import butterknife.ButterKnife
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState
import seigneur.gauvain.mycourt.ui.shots.list.data.Status


class NetworkStateViewHolder(
        itemView: View,
        shotItemCallback: ShotItemCallback) : RecyclerView.ViewHolder(itemView) {

    @BindView(R.id.errorMessageTextView)
    lateinit var errorMessageTextView: TextView

    @BindView(R.id.retryLoadingButton)
    lateinit var retryLoadingButton: Button

    @BindView(R.id.loadingProgressBar)
    lateinit var loadingProgressBar: ProgressBar

    init {
        ButterKnife.bind(this, itemView)
        retryLoadingButton.setOnClickListener { _ -> shotItemCallback.retry() }
    }

    fun bindTo(networkState: NetworkState) {
        //error message
        errorMessageTextView.visibility =
                if (networkState.message.isEmpty())
                    View.VISIBLE
                else
                    View.GONE
        errorMessageTextView.text = networkState.message

        //loading and retry
        retryLoadingButton.visibility = if (networkState.status === Status.FAILED) View.VISIBLE else View.GONE
        loadingProgressBar.visibility = if (networkState.status === Status.RUNNING) View.VISIBLE else View.GONE
    }

    companion object {

        fun create(parent: ViewGroup, shotItemCallback: ShotItemCallback): NetworkStateViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_network_state, parent, false)
            return NetworkStateViewHolder(view, shotItemCallback)
        }
    }

}
