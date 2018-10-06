package seigneur.gauvain.mycourt.ui.shotDetail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity;
import seigneur.gauvain.mycourt.utils.ImageUtils;
import seigneur.gauvain.mycourt.utils.MyColorUtils;
import seigneur.gauvain.mycourt.utils.MyTextUtils;
import static seigneur.gauvain.mycourt.utils.MathUtils.convertPixelsToDp;

public class ShotDetailActivity extends BaseActivity {

    private float differenceHeightBigShotMiniShot;
    private float differenceWidthBigShotMiniShot;
    private float ratioBigShotMiniShot;
    private Float mTargetElevation;

    /*@Inject
    ShotDetailPresenter mShotDetailPresenter;*/

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    ShotDetailViewModel mShotDetailViewModel;

    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;

    @BindView(R.id.shot_title)
    TextView shotTitle;

    @BindView(R.id.shot_description)
    TextView shotDescription;

    @BindView(R.id.shot_update_date)
    TextView shotUpdate;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.dummy_fourthree_view)
    ImageView emptyFourthreeView;

    @BindView(R.id.shot_image)
    ImageView picture;

    @BindView(R.id.black_back_arrow)
    ImageView blackBackArrow;

    @BindView(R.id.custom_status_bar_background)
    View customStatusBarbackground;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.shot_tags_list)
    RecyclerView shotTags;

    private int twentyFourDip;
    private boolean isLightStatusBar = false;
    private View view;
    private Window window;
    private TagListAdapter mTagListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        setContentView(R.layout.activity_shot_detail);
        window = this.getWindow();
        view = window.getDecorView();
        twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, this.getResources().getDisplayMetrics());
        ButterKnife.bind(this);

        mShotDetailViewModel = ViewModelProviders.of(this, viewModelFactory).get(ShotDetailViewModel.class);
        mShotDetailViewModel.init();
        //listen livedata
        suscribeToLivedata(mShotDetailViewModel);
        //listen single events
        suscribetoSingleEvents(mShotDetailViewModel);

    }

    private void suscribeToLivedata(ShotDetailViewModel viewModel) {
        viewModel.getShot()
                .observe(
                        this,
                        shot -> {
                            if (shot!=null) {
                                loadShotImage(shot);
                            }
                        }
                );
    }

    private void suscribetoSingleEvents(ShotDetailViewModel viewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.getEditClickedEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void method) {
                goToShotEdition();
            }
        });

    }

    public void loadShotImage(Shot shot) {
        loadShotImage(true, shot);
    }

    private void setUpShotInfoView(Shot shot, boolean isImageReady, Drawable drawable) {
        startPostponedEnterTransition();
        showPaletteShot(false);
        if (isImageReady && drawable!=null)
            adaptColorToShot(drawable);
        initImageScrollBehavior();
        setUpShotInfo(shot);
    }

    @OnClick(R.id.fab)
    public void goToEdition() {
        mShotDetailViewModel.onEditClicked();
    }

    public void goToShotEdition() {
        Intent intent = new Intent(this, EditShotActivity.class);
        startActivity(intent);
    }

    public void showErrorView(boolean visible) {
        if(visible)
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
    }


    public void showPaletteShot(boolean isVisible) {
        //if isVisible show layout and show palette color!
    }


    public void adaptColorToShot(Drawable resource) {
        Bitmap bitmap = ImageUtils.drawableToBitmap(resource);
        recolorStatusBar(bitmap);
    }


    public void initImageScrollBehavior() {
        initMathData();
        appBarLayout.addOnOffsetChangedListener(appBarOffsetListener);
    }


    public void setUpShotInfo(Shot shot) {
        shotTitle.setText(shot.getTitle());
        shotDescription.setText(shotDescription(shot));
        shotUpdate.setText("created at:xx xx xx");
        setUpTagList(shot);
    }

    public void showEditionResult(int result) {

    }

    private void recolorStatusBar(Bitmap  bitmap) {
        final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                24, this.getResources().getDisplayMetrics());
       // Bitmap bitmap = BitmapFactory.decodeResource(drawable);
        Palette.from(bitmap)
                .clearFilters()
                .setRegion(0,0, bitmap.getWidth(),twentyFourDip)
                .generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                //work with the palette here
                int defaultValue = 0x000000;
                int dominantColor = palette.getDominantColor(defaultValue);
                int vibrantColor = palette.getVibrantColor(defaultValue);
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                Palette.Swatch dominantSwatchSwatch = palette.getDominantSwatch();
                // finally change the color
                if(dominantSwatchSwatch != null){
                    Animation fadeIn = new AlphaAnimation(0, 1);
                    fadeIn.setInterpolator(new DecelerateInterpolator());
                    fadeIn.setDuration(350);
                    customStatusBarbackground.startAnimation(fadeIn);
                    customStatusBarbackground.setBackgroundColor(dominantSwatchSwatch.getRgb());
                }

                if (!MyColorUtils.isDark(dominantColor)) {
                    isLightStatusBar=true;
                    blackBackArrow.setAlpha(1.0f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        MyColorUtils.setLightStatusBar(view);
                    }
                } else {
                    blackBackArrow.setAlpha(0f);
                }

            }
        });
    }

    private void loadShotImage(final boolean isTransactionPostponed, Shot shot) {
        if (isTransactionPostponed)
            postponeEnterTransition();
        /**
         *  AS shot image is loaded from Glide ressource, put listener to define when to start startPostponedEnterTransition
         */
        Glide
                .with(this)
                .asDrawable()
                .load(Uri.parse(shot.getImageUrl()))
                //.apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        //do something! - make an API call or load another image and call mShotDetailPresenter.onShotImageAvailable();
                        if (isTransactionPostponed) {
                            setUpShotInfoView(shot, false, null);
                        }

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (isTransactionPostponed) {
                            //TODO - singleLive event
                            setUpShotInfoView(shot, true, resource);
                        }
                        return false;
                    }
                })
                .into(picture);
    }

    private String shotDescription(Shot shot) {
        if (shot.getDescription()!=null) {
            String htmlFormatDescription = Html.fromHtml(shot.getDescription()).toString();
            return MyTextUtils.noTrailingwhiteLines(htmlFormatDescription).toString();
        } else {
            return "no description defined";
        }
    }

    public AppBarLayout.OnOffsetChangedListener appBarOffsetListener =
            new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            final int 	vTotalScrollRange 	= appBarLayout.getTotalScrollRange();
            final float vRatio 				= ((float)vTotalScrollRange + verticalOffset) / vTotalScrollRange;
            float offsetAlpha = (appBarLayout.getY() / appBarLayout.getTotalScrollRange());
            float imageIntoolbarPaddingIng =  getResources().getDimension(R.dimen.shot_image_toolbar_padding);
            customStatusBarbackground.setAlpha(vRatio);

            if (isLightStatusBar) {
                blackBackArrow.setAlpha(vRatio);
                if (vRatio<0.50) {
                    MyColorUtils.clearLightStatusBar(view);
                } else {
                    MyColorUtils.setLightStatusBar(view);
                }
            }

            /**
             * Horizontal Transition effect on ShotImage
             */
            float transitionX = vRatio + (differenceWidthBigShotMiniShot-(differenceWidthBigShotMiniShot*vRatio));
            float transitionXWithPadding = transitionX -(vRatio + (-imageIntoolbarPaddingIng*(vRatio-1)));
            picture.setX(transitionXWithPadding);
            /**
             * Vertical Transition effect on ShotImage
             */
            float transitionY = vRatio + (differenceHeightBigShotMiniShot*(vRatio-1))+twentyFourDip;
            float transitionYWithPadding = transitionY -(vRatio + (imageIntoolbarPaddingIng*(vRatio-1)));
            //float transitionYWithPadding = transitionY +(vRatio + (-imageIntoolbarPaddingIng*(vRatio-1)));
            picture.setY(transitionYWithPadding);
            /**
             * scale effect effect on ShotImage
             */
            float scale = vRatio + (ratioBigShotMiniShot-(ratioBigShotMiniShot*vRatio));
            picture.setScaleX(scale);
            picture.setScaleY(scale);

            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // Collapse
                picture.setOutlineProvider(ViewOutlineProvider.PADDED_BOUNDS);
            } else if (verticalOffset == 0) {
                // Expanded
                picture.setOutlineProvider(null);
            } else {
                // Somewhere in between
                picture.setOutlineProvider(null);
            }
        }
    };

    private void initMathData() {
        //ini math data
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics ();
        display.getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        float screenHeightInDp = convertPixelsToDp(screenHeight,ShotDetailActivity.this);
        int emptyFourthreeViewHeight = emptyFourthreeView.getMeasuredHeight();
        int emptyFourthreeViewWidth = emptyFourthreeView.getMeasuredWidth();
        float emptyFourthreeViewHeightInDP =  convertPixelsToDp(emptyFourthreeViewHeight,ShotDetailActivity.this);
        float shotImageToolbarHeightinDP =  convertPixelsToDp(getResources().getDimension(R.dimen.shot_image_toolbar_height),ShotDetailActivity.this);
        float shotImageToolbarHeightinPX =  getResources().getDimension(R.dimen.shot_image_toolbar_height);
        float ratioBigShotScreen = emptyFourthreeViewHeightInDP/screenHeightInDp;
        float ratioMiniShotScreen = shotImageToolbarHeightinDP/screenHeightInDp;

        ratioBigShotMiniShot =ratioMiniShotScreen/ratioBigShotScreen;
        differenceWidthBigShotMiniShot = (emptyFourthreeViewWidth/2)-((shotImageToolbarHeightinPX*1.25f)/2);//keep FourThreeImageView ratio
        differenceHeightBigShotMiniShot = (emptyFourthreeViewHeight/2)-(shotImageToolbarHeightinPX/2);
    }

    private void setUpTagList(Shot shot) {
        if(shot.getTagList()!=null && shot.getTagList().size()>0) {
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setJustifyContent(JustifyContent.FLEX_START);
            shotTags.setLayoutManager(layoutManager);
            mTagListAdapter = new TagListAdapter(this, shot.getTagList());
            shotTags.setAdapter(mTagListAdapter);
        }
    }

}
