package seigneur.gauvain.mycourt.ui.shots.view;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import seigneur.gauvain.mycourt.ui.shots.recyclerview.PaginationAdapter;
import seigneur.gauvain.mycourt.ui.shots.recyclerview.PaginationAdapterCallback;
import seigneur.gauvain.mycourt.ui.shots.recyclerview.PaginationScrollListener;
import seigneur.gauvain.mycourt.ui.shots.presenter.ShotsPresenter;
import seigneur.gauvain.mycourt.ui.shotDetail.view.ShotDetailActivity;

/**
 * Created by gse on 22/11/2017.
 */
public class ShotsFragment extends BaseFragment implements ShotsView {

    @Inject
    ShotsPresenter mShotsPresenter;
    @BindView(R.id.swipe_refresh_shots)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.rv_shots)
    RecyclerView recyclerView;

    private GridLayoutManager mGridLayoutManager;
    //pagination tests
    private static final int PAGE_START = 1;
    private int currentPage = PAGE_START;
    private PaginationAdapter adapter;
    private PaginationAdapterCallback adapterCallback;
    private ProgressBar progressBar;

    /*
     * FRAGMENT LIFE CYCLE
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //first time created
        adapterCallback = new PaginationAdapterCallback() {
            @Override
            public void retryPageLoad() {
                mShotsPresenter.onLoadNextPage(currentPage);
            }
            @Override
            public void onShotClicked(Shot shot,int position) {
                mShotsPresenter.onShotClicked(shot, position);
            }
        };
        adapter = new PaginationAdapter(getContext(),adapterCallback);
        mGridLayoutManager = new GridLayoutManager(getContext(),2);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(adapter.getItemViewType(position)){
                    case PaginationAdapter.ITEM:
                        return position == 0 ? 2 : 1;
                    case PaginationAdapter.LOADING:
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
        mShotsPresenter.onAttach();
        recyclerView.setLayoutManager(mGridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new PaginationScrollListener(mGridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                mShotsPresenter.onLoading();
                currentPage += 1;
                mShotsPresenter.onLoadNextPage(currentPage);
            }

            @Override
            public boolean isLastPage() {
                return mShotsPresenter.onLastPageReached();
            }

            @Override
            public boolean isLoading() {
                return mShotsPresenter.isLoading();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = PAGE_START;
                //todo - must limit the reQUEST BY ADDING a HEADER ? and use diff utils ?
                mShotsPresenter.onLoadRefresh(PAGE_START);
            }
        });
        // Scheme colors for animation
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorAccent)
                //getResources().getColor(android.R.color.holo_green_light),
               // getResources().getColor(android.R.color.holo_orange_light),
                //getResources().getColor(android.R.color.holo_red_light)
        );

        mShotsPresenter.onLoadFirstPage(currentPage);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        mShotsPresenter.onDetach();
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
        if (isVisible)
            adapter.addLoadingFooter();
        else
            adapter.removeLoadingFooter();
    }

    @Override
    public void showFirstFecthErrorView(boolean isVisible) {
        if (isVisible)
            Toast.makeText(activity, "error fetch first list!!!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNextFetchError(boolean isVisible, String error) {
        if (isVisible)
            adapter.showRetry(isVisible, error);
    }

    @Override
    public void showEmptyListView(boolean visible) {
        if(visible) {
            Toast.makeText(activity, "list empty", Toast.LENGTH_SHORT).show();
        }
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

}