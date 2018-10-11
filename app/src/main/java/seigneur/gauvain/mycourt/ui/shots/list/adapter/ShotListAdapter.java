package seigneur.gauvain.mycourt.ui.shots.list.adapter;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


import java.util.Objects;

import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.shots.list.data.NetworkState;

public class ShotListAdapter extends PagedListAdapter<Shot, RecyclerView.ViewHolder> {

    private NetworkState networkState;

    private ShotItemCallback shotItemCallback;

    public static final int ITEM = 0;
    public static final int LOADING = 1;

    private int pos;

    public ShotListAdapter(ShotItemCallback shotItemCallback) {
        super(UserDiffCallback);
        this.shotItemCallback = shotItemCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM:
                return ShotViewHolder.create(parent,shotItemCallback);
            case LOADING:
                return NetworkStateViewHolder.create(parent, shotItemCallback);
            default:
                throw new IllegalArgumentException("unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM:
                ((ShotViewHolder) holder).bindTo(getItem(position));
                break;
            case LOADING:
                ((NetworkStateViewHolder) holder).bindTo(networkState);
                break;
        }
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState != NetworkState.LOADED;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasExtraRow() && position == getItemCount() - 1) {
            return LOADING;
        } else {
            return ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (hasExtraRow() ? 1 : 0);
    }

    public Shot getShotClicked(int pos) {
        return getItem(pos);
    }

    /**
     * Set the current network state to the adapter
     * but this work only after the initial load
     * and the adapter already have list to add new loading raw to it
     * so the initial loading state the activity responsible for handle it
     *
     * @param newNetworkState the new network state
     */
    public void setNetworkState(NetworkState newNetworkState) {
        if (getCurrentList() != null) {
            if (getCurrentList().size() != 0) {
                NetworkState previousState = this.networkState;
                boolean hadExtraRow = hasExtraRow();
                this.networkState = newNetworkState;
                boolean hasExtraRow = hasExtraRow();
                if (hadExtraRow != hasExtraRow) {
                    if (hadExtraRow) {
                        notifyItemRemoved(super.getItemCount());
                    } else {
                        notifyItemInserted(super.getItemCount());
                    }
                } else if (hasExtraRow && previousState != newNetworkState) {
                    notifyItemChanged(getItemCount() - 1);
                }
            }
        }
    }

    private static DiffUtil.ItemCallback<Shot> UserDiffCallback = new DiffUtil.ItemCallback<Shot>() {
        @Override
        public boolean areItemsTheSame(@NonNull Shot oldItem, @NonNull Shot newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Shot oldItem, @NonNull Shot newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };


}