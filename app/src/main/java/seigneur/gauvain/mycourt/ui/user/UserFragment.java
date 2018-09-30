package seigneur.gauvain.mycourt.ui.user;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.User;
import timber.log.Timber;

import static seigneur.gauvain.mycourt.utils.MathUtils.convertPixelsToDp;


/**
 * Created by gse on 22/11/2017.
 */
public class UserFragment extends Fragment  {

    @Inject
    Application mApplication;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private UserViewModel mUserViewModel;

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.login)
    TextView login;

    @BindView(R.id.avatar)
    ImageView avatar;

    @BindView(R.id.label_pro)
    ImageView proLabel;

    @BindView(R.id.bio)
    TextView bio;

    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.other_user_info_app_bar_layout)
    LinearLayout userTextInfoInAppbar;

    @BindView(R.id.location)
    TextView userLocation;

    @BindView(R.id.followers)
    TextView userFollowers;

    @BindView(R.id.rv_user_links)
    RecyclerView mUserLinksList;

    private UserLinksAdapter mUserLinksAdapter;

    private int screenWidth;

    private Unbinder mUnbinder;
    public View mRootview;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        Timber.d("onAttach");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
        }
        super.onAttach(activity);
    }

    @Override
    public void onAttach(Context context) {
        Timber.d("onAttach");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);

        }
        super.onAttach(context);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
        //provide ViewModel
        mUserViewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel.class);
        //fetch user if is not already fetched - todo make api prior
        mUserViewModel.init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView");
        mRootview = inflater.inflate(getFragmentLayout(), container, false );
        mUnbinder= ButterKnife.bind(this, mRootview);
        initMathData();

        return mRootview;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        appBarLayout.addOnOffsetChangedListener(appBarOffsetListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //listen livedata
        mUserViewModel.getUser()
                .observe(
                        this,
                        new Observer<User>() {
                            @Override
                            public void onChanged(@Nullable User user) {
                                if (user!=null) {
                                    setUpUserAccountInfo(user);
                                    setUserPicture(user);
                                    showUserLinks(user);
                                } else {
                                    showNoUserFoundView(true);
                                }
                            }
                        }
                );

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    protected int getFragmentLayout() {
        return R.layout.fragment_user;
    }


    public void showNoConnectionView(boolean visible) {
        if(visible) {
            Toast.makeText(getActivity(), "no connection", Toast.LENGTH_SHORT).show();
        }
    }


    public void setUpUserAccountInfo(User user){
        name.setText(user.getName());
        login.setText(user.getLogin());
        userLocation.setText(user.getLocation());
        if(user.getFollowers_count() >0)
            userFollowers.setText(user.getFollowers_count()+" followers");
        else
            userFollowers.setText("no follower");
        if (!user.getBio().isEmpty()) {
            bio.setText(Html.fromHtml(user.getBio()));
        } else
            bio.setText("You didn't defined your bio");
        if (user.isPro())
            proLabel.setVisibility(View.VISIBLE);
    }


    public void setUserPicture(User user) {
        Glide.with(mApplication)
                .load(Uri.parse(user.getAvatar_url()))
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.avatar_place_holder)
                        .error(R.drawable.avatar_place_holder)
                )
                .into(avatar);
    }


    public void showNoTeamsView(boolean visible) {

    }


    public void showUserLinks(User user) {
        ArrayList userLinks = new ArrayList();
        userLinks.add(mapToList(user.getLinks()));
        //mUserLinksList.setLayoutManager(new LinearLayoutManager(getContext()));
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        mUserLinksList.setLayoutManager(layoutManager);
        mUserLinksAdapter = new UserLinksAdapter(getContext(), user.getLinks());
        mUserLinksList.setAdapter(mUserLinksAdapter);
    }


    public void showNoLinksView(boolean visible) {

    }


    public void showNoUserFoundView(boolean visible) {
        Toast.makeText(mApplication, "no user available", Toast.LENGTH_SHORT).show();
    }

    /*
    *********************************************************************************
    * Internal methods
    *********************************************************************************/

    /**
     * add to appbar a listener for scroll change in order to provide some
     * nice animation on scroll
     */
    private AppBarLayout.OnOffsetChangedListener appBarOffsetListener =
            new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    final int 	vTotalScrollRange 	= appBarLayout.getTotalScrollRange();
                    final float vRatio 				= ((float)vTotalScrollRange + verticalOffset) / vTotalScrollRange;

                    float avatarSize =  avatar.getWidth();
                    float avatarBigSize =  getResources().getDimension(R.dimen.avatar_bounds);
                    float avatarMiniSize =  getResources().getDimension(R.dimen.avatar_mini);

                    float ratioAvatarSizes = avatarMiniSize/avatarBigSize;
                    float diffBigMiniAvatarSize= avatarBigSize-avatarMiniSize;

                    float avatarMiniPaddingTopBottom = getResources().getDimension(R.dimen.avatar_mini_padding_top_bottom);
                    float avatarExpandTopMargin=  getResources().getDimension(R.dimen.avatar_big_margin_top);
                    float avatarLeftMarginOnCollapsed=  getResources().getDimension(R.dimen.padding_16);
                    float nameExpandTopMargin=  getResources().getDimension(R.dimen.user_name_expand_top_margin);
                    float nameMargingLeftOnCollapsed=  getResources().getDimension(R.dimen.user_name_expand_left_margin);

                    int nameWidth = name.getWidth();
                    int nameHeight = name.getHeight();
                    int diffToolbarHeightNameheight = toolbar.getHeight()-nameHeight;

                    /**
                     * Horizontal Transition effect
                     */
                    float transitionX = (vRatio *((screenWidth/2)-(avatarBigSize/2))-
                            ((diffBigMiniAvatarSize/2)-avatarLeftMarginOnCollapsed - (((diffBigMiniAvatarSize/2)-avatarLeftMarginOnCollapsed) *(vRatio))));
                    avatar.setX(transitionX);

                    float transitionXTitle = (vRatio *((screenWidth/2)-(nameWidth/2))+
                            ((nameMargingLeftOnCollapsed)-(nameMargingLeftOnCollapsed)*vRatio));
                    avatar.setX(transitionX);
                    name.setX(transitionXTitle);
                    /*float transitionXTitle = (vRatio - ((nameWidth/2)-((nameWidth/2)*vRatio))) +
                            (((nameWidth/4)+nameMargingLeftOnCollapsed)-(((nameWidth/4)+nameMargingLeftOnCollapsed)*vRatio));
                    name.setX(transitionXTitle);*/

                    /**
                     * scale effect effect on Avatar
                     */
                    float scale = vRatio + (ratioAvatarSizes-(ratioAvatarSizes*vRatio));
                    avatar.setScaleX(scale);
                    avatar.setScaleY(scale);

                    /**
                     * Vertical Transition effect
                     */
                    float transitionY = (vRatio * avatarExpandTopMargin)//Manage padding when appBar is expanded
                            - (diffBigMiniAvatarSize/2 - (diffBigMiniAvatarSize/2 *(vRatio))) //Manage size diff
                            + (avatarMiniPaddingTopBottom -(avatarMiniPaddingTopBottom*vRatio)); //Manage padding when appBar is collapsed
                    avatar.setY(transitionY);

                    float transitionNameY = (vRatio * nameExpandTopMargin)//Manage padding when appBar is expanded
                                            + (diffToolbarHeightNameheight/2 -((diffToolbarHeightNameheight/2)*vRatio)); //Manage padding when appBar is collapsed
                    userTextInfoInAppbar.setY(transitionNameY);
                    //name.setY(transitionNameY);
                    userLocation.setAlpha(vRatio);
                    userFollowers.setAlpha(vRatio);
                }
            };

    /**
     * init mathematics data for scroll animation
     * init these only when view was created in order to avoid some weird behavior on first opening
     */
    public void initMathData() {
        //ini math data
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics ();
        display.getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        float screenHeightInDp = convertPixelsToDp(screenHeight,getActivity());
        screenWidth = displayMetrics.widthPixels;
        float screenWidthDP = convertPixelsToDp(screenWidth,getActivity());
    }

    /**
     * Transform a key/value map to a a list of string using key
     * for links
     * @param env - map fetched
     * @return - list of links
     */
    public static List<String> mapToList(Map<String, String> env) {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, String> entry : env.entrySet())
            result.add(entry.getKey() /*+ " " + entry.getValue()*/);
        return result;
    }

}