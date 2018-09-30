package seigneur.gauvain.mycourt.ui.shots.view;

import android.app.Activity;
import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.base.BaseFragment;
import seigneur.gauvain.mycourt.ui.shots.ShotsViewModel;
import seigneur.gauvain.mycourt.ui.shots.recyclerview.ShotListAdapter;
import seigneur.gauvain.mycourt.ui.shots.recyclerview.ShotListCallback;
import seigneur.gauvain.mycourt.ui.shots.recyclerview.ShotScrollListener;
import seigneur.gauvain.mycourt.ui.shotDetail.ShotDetailActivity;
import seigneur.gauvain.mycourt.utils.Constants;
import timber.log.Timber;

/**
 * Created by gse on 22/11/2017.
 */
public class ShotsFragment extends BaseFragment implements ShotsView, ShotListCallback {

    /*@Inject
    ShotsPresenter mShotsPresenter;*/

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    ShotsViewModel mShotsViewModel;

    @BindView(R.id.swipe_refresh_shots)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.rv_shots)
    RecyclerView recyclerView;

    private GridLayoutManager mGridLayoutManager;
    //pagination tests
    private static final int PAGE_START = 1;
    private int currentPage = PAGE_START;
    private ShotListAdapter adapter;
    //private ShotListCallback adapterCallback;
    private ProgressBar progressBar;
    boolean isLastpage=false;
    boolean isLoading=false;

    /*
     * FRAGMENT LIFE CYCLE
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShotsViewModel = ViewModelProviders.of(this, viewModelFactory).get(ShotsViewModel.class);
        mShotsViewModel.onLoadFirstPage();
        adapter = new ShotListAdapter(getContext(), this);
        mGridLayoutManager = new GridLayoutManager(getContext(),2);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(adapter.getItemViewType(position)){
                    case ShotListAdapter.ITEM:
                        return position == 0 ? 2 : 1;
                    case ShotListAdapter.LOADING:
                        return 2;
                    default:
                        return 1;
                }
            }
        });

    }

    //Overridden from BaseFragment
    @Override
    public void onCreateView(View rootView, Bundle savedInstanceState) {
        //bindView here
        ButterKnife.bind(this, rootView);

        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        recyclerView.setItemAnimator(animator);
        recyclerView.setLayoutManager(mGridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new ShotScrollListener(mGridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                mShotsViewModel.onLoadNextPage();
            }

            @Override
            public boolean isLastPage() {
                return isLastpage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mShotsViewModel.onLoadRefresh();
            }
        });
        // Scheme colors for animation
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorAccent)
        );

        subscribeToLiveData(mShotsViewModel);
        subscribeToSingleEvents(mShotsViewModel);
    }

    private void subscribeToLiveData(ShotsViewModel shotsViewModel) {

        mShotsViewModel.geShotsFetched().observe(
                this,
                new Observer<List<Shot>>() {
                    @Override
                    public void onChanged(@Nullable List<Shot> shots) {
                        if (shots!=null) {
                            addShots(shots);
                        } else {

                        }
                    }
                }
        );

        mShotsViewModel.getListFooterState().observe(
                this,
                state -> {
                    if (state!=null) {
                        Timber.d("footer state change:" +state);
                        switch (state) {
                            case Constants.FOOTER_STATE_LOADING :
                                    isLoading=true;
                                    isLastpage=false;
                                    showLoadingFooter(true);
                                    showEndListReached(false);
                                showNextFetchError(false, null);
                                break;
                            case Constants.FOOTER_STATE_NOT_LOADING :
                                isLoading=false;
                                isLastpage=false;
                                showLoadingFooter(false);
                                showEndListReached(false);
                                showNextFetchError(false, null);
                                break;
                                case Constants.FOOTER_STATE_ERROR :
                                    isLoading=false;
                                    isLastpage=false;
                                    showLoadingFooter(false);
                                    showEndListReached(false);
                                    showNextFetchError(true, "error");
                                break;
                                case Constants.FOOTER_STATE_END :
                                    isLoading=false;
                                    isLastpage=true;
                                    showLoadingFooter(false);
                                    showEndListReached(true);
                                    showNextFetchError(false, null);
                                break;

                        }
                    }

                }
        );

    }

    private void subscribeToSingleEvents(ShotsViewModel shotsViewModel) {
        shotsViewModel.getRefreshEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void methdod) {
                clearShots();
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    /**
     * BASE FRAGMENT METHODS
     */
    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_shots;
    }

    @Override
    public void addShots(final List<Shot> shots) {
        adapter.addAll(shots);
    }

    @Override
    public void showLoadingFooter(boolean isVisible) {
        /*if (isVisible)
            adapter.addLoadingFooter();
        else
           adapter.removeLoadingFooter();*/
    }

    @Override
    public void showFirstFecthErrorView(boolean isVisible) {
        if (isVisible)
            Toast.makeText(activity, "error fetch first list!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNextFetchError(boolean isVisible, String error) {
        adapter.showRetry(isVisible, error);
    }

    @Override
    public void showEndListReached(boolean isVisible) {
        Timber.tag("newrequest").d("showEndListReached called");
        adapter.showEndListMessage(isVisible);
    }

    @Override
    public void showEmptyListView(boolean visible) {
        if(visible)
            Toast.makeText(activity, "list empty", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void stopRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void clearShots() {
        adapter.clear();
    }


    @Override
    public void goToShotDetail(Shot shot, int position) {
        ActivityOptions options = null;
        Intent i = new Intent(getActivity(), ShotDetailActivity.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            options = ActivityOptions.makeSceneTransitionAnimation((Activity) getActivity(),
                    mGridLayoutManager.findViewByPosition(position),
                    getActivity().getString(R.string.shot_transition_name));
            getContext().startActivity(i, options.toBundle());
        }
    }

    @Override
    public void retryPageLoad() {
        //adapter.showRetry(false, null); //to make it in View and call it in presenter
        //mShotsPresenter.onLoadNextPage(currentPage);  //TODO - VIEWMODEL
    }
    @Override
    public void onShotClicked(int position) {
        Shot shotItem = adapter.getItem(position);
        //mShotsPresenter.onShotClicked(shotItem, position);  //TODO - VIEWMODEL
    }

}
