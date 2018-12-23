package seigneur.gauvain.mycourt.ui.shotDetail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import dagger.android.AndroidInjection
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView
import seigneur.gauvain.mycourt.ui.widget.ParallaxImageView
import seigneur.gauvain.mycourt.utils.image.ImageUtils
import seigneur.gauvain.mycourt.utils.MyColorUtils
import seigneur.gauvain.mycourt.utils.MyTextUtils
import seigneur.gauvain.mycourt.utils.MathUtils.convertPixelsToDp
import timber.log.Timber

class ShotDetailActivity : BaseActivity() {

    private var differenceHeightBigShotMiniShot: Float = 0.toFloat()
    private var differenceWidthBigShotMiniShot: Float = 0.toFloat()
    private var ratioBigShotMiniShot: Float = 0.toFloat()

    private var scrolldiff: Float = 0.toFloat()
    val zvalue:Float by lazy {
        resources.getDimension(R.dimen.shot_detail_toolbar_height)
    }
    var yvalue:Float =0.toFloat()
    //private val mTargetElevation: Float? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mShotDetailViewModel: ShotDetailViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ShotDetailViewModel::class.java)
    }

    @BindView(R.id.app_bar)
    lateinit var appBarLayout: AppBarLayout

    @BindView(R.id.shot_title)
    lateinit var shotTitle: TextView

    @BindView(R.id.shot_description)
    lateinit var shotDescription: TextView

    @BindView(R.id.shot_update_date)
    lateinit var shotUpdate: TextView

    @BindView(R.id.toolbar)
    lateinit var mToolbar: Toolbar

    @BindView(R.id.dummy_fourthree_view)
    lateinit var emptyFourthreeView: FourThreeImageView

    @BindView(R.id.shot_image)
    lateinit var picture: FourThreeImageView

    @BindView(R.id.image_scrim)
    lateinit var imgScrim: View

    @BindView(R.id.white_back_arrow)
    lateinit var backArrow: ImageView

    @BindView(R.id.custom_status_bar_background)
    lateinit var customStatusBarbackground: View

    @BindView(R.id.fab)
    lateinit var fab: FloatingActionButton

    @BindView(R.id.shot_tags_list)
    lateinit var shotTags: RecyclerView

    private var statusbarheight: Int = 0
    private var isLightStatusBar = false
    val OneHundreddip:Int by lazy {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, this.resources.displayMetrics).toInt()
    }

    private val mWindow: Window by lazy {
        this.window
    }

    private val mView: View by lazy {
        mWindow.decorView
    }

    private var mTagListAdapter: TagListAdapter? = null

    var appBarOffsetListener: AppBarLayout.OnOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        val vTotalScrollRange = appBarLayout.totalScrollRange
        val vRatio = (vTotalScrollRange.toFloat() + verticalOffset) / vTotalScrollRange
        val offsetAlpha = appBarLayout.y / appBarLayout.totalScrollRange
        val imageIntoolbarPaddingIng = resources.getDimension(R.dimen.shot_image_toolbar_padding)
       // manageImageAspectOldVersion(verticalOffset, vRatio, imageIntoolbarPaddingIng)
        manageImageAspectNewStyle(vRatio)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        setContentView(R.layout.activity_shot_detail)
        ButterKnife.bind(this)
        mShotDetailViewModel.init()
        //listen livedata
        subscribeToLiveData(mShotDetailViewModel)
        //listen single events
        subscribeToSingleEvents(mShotDetailViewModel)

    }

    override fun onBackPressed() {
        //super.onBackPressed()
        appBarLayout.setExpanded(true)
        finishAfterTransition()
    }

    private fun subscribeToLiveData(viewModel: ShotDetailViewModel) {
        viewModel.shot
                .observe(
                        this,
                        Observer<Shot?> {
                            shot -> loadShotImage(shot)
                        }

                )
    }

    private fun subscribeToSingleEvents(viewModel: ShotDetailViewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.editClickedEvent.observe(this, Observer { goToShotEdition() })

    }

    private fun loadShotImage(shot: Shot?) {
        loadShotImage(true, shot)
    }

    private fun setUpShotInfoView(shot: Shot?, isImageReady: Boolean, drawable: Drawable?) {
        startPostponedEnterTransition()
        showPaletteShot(false)
        if (isImageReady && drawable != null)
            adaptColorToShot(drawable)
        initImageScrollBehavior()
        setUpShotInfo(shot)
    }

    @OnClick(R.id.fab)
    fun goToEdition() {
        mShotDetailViewModel.onEditClicked()
    }

    private fun goToShotEdition() {
        val intent = Intent(this, EditShotActivity::class.java)
        startActivity(intent)
    }

    private fun showErrorView(visible: Boolean) {
        if (visible)
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
    }


    private fun showPaletteShot(isVisible: Boolean) {
        //if isVisible show layout and show palette color!
    }


    private fun adaptColorToShot(resource: Drawable) {
        val bitmap = ImageUtils.drawableToBitmap(resource)
        recolorStatusBar(bitmap)
        recolorShadowColor(bitmap)
    }


    private fun initImageScrollBehavior() {
        //initMathDataForImageAspectOdStyle()
        initMathDataForImageAspectNewStyle()
        appBarLayout.addOnOffsetChangedListener(appBarOffsetListener)
    }


    private fun setUpShotInfo(shot: Shot?) {
        shotTitle.text = shot!!.title
        shotDescription.text = shotDescription(shot)
        shotUpdate.text = "created at:xx xx xx"
        setUpTagList(shot)
    }

    private fun showEditionResult(result: Int) {
    }

    private fun recolorStatusBar(bitmap: Bitmap) {
        val twentyFourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                24f, this.resources.displayMetrics).toInt()
        // Bitmap bitmap = BitmapFactory.decodeResource(drawable);
        Palette.from(bitmap)
                .clearFilters()
                .setRegion(0, 0, bitmap.width, twentyFourDip)
                .generate { palette ->
                    //work with the palette here
                    val defaultValue = 0x000000
                    val dominantColor = palette!!.getDominantColor(defaultValue)
                    val dominantSwatchSwatch = palette.dominantSwatch
                    val vibrantSwatch = palette.vibrantSwatch
                    // finally change the color
                    if (dominantSwatchSwatch != null) {
                        imgScrim.setBackgroundColor(dominantSwatchSwatch.rgb)
                        val fadeIn = AlphaAnimation(0f, 1f)
                        fadeIn.interpolator = DecelerateInterpolator()
                        fadeIn.duration = 350
                        customStatusBarbackground.startAnimation(fadeIn)
                        customStatusBarbackground.setBackgroundColor(dominantSwatchSwatch.rgb)
                    }

                    if (!MyColorUtils.isDark(dominantColor)) {
                        isLightStatusBar = true
                        //blackBackArrow.alpha = 1.0f
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            MyColorUtils.setLightStatusBar(mView)
                        }
                    } else {
                    }
                }
    }

    private fun recolorShadowColor(bitmap: Bitmap) {
        val twentyFourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                24f, this.resources.displayMetrics).toInt()
        Palette.from(bitmap)
                .clearFilters()
                .setRegion(0, bitmap.height-twentyFourDip, bitmap.width, bitmap.height)
                .generate { palette ->
                    //work with the palette here
                    val dominantSwatch = palette!!.dominantSwatch
                    // finally change the color
                    if (dominantSwatch != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            appBarLayout.outlineAmbientShadowColor = dominantSwatch.rgb
                            appBarLayout.outlineSpotShadowColor = dominantSwatch.rgb
                            val opHexColor = String.format("#%06X", 0xFFFFFF and dominantSwatch.rgb)
                            Timber.d("dominantSwatch rgb: "+opHexColor)
                        }
                    } else {
                        Timber.d("dominantSwatch is null")
                    }
                }
    }

    private fun loadShotImage(isTransactionPostponed: Boolean, shot: Shot?) {
        if (isTransactionPostponed)
            postponeEnterTransition()
        /**
         * AS shot image is loaded from Glide ressource, put listener to define when to start startPostponedEnterTransition
         */
        Glide
                .with(this)
                .asDrawable()
                .load(Uri.parse(shot!!.imageUrl))
                //.apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        //do something! - make an API call or load another image and call mShotDetailPresenter.onShotImageAvailable();
                        if (isTransactionPostponed) {
                            setUpShotInfoView(shot, false, null)
                        }

                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        if (isTransactionPostponed) {
                            //TODO - singleLive event
                            setUpShotInfoView(shot, true, resource)
                        }
                        return false
                    }
                })
                .into(picture)
    }

    private fun shotDescription(shot: Shot): String {
        if (shot.description != null) {
            val htmlFormatDescription = Html.fromHtml(shot.description).toString()
            return MyTextUtils.noTrailingwhiteLines(htmlFormatDescription).toString()
        } else {
            return "no description defined"
        }
    }


    private fun setUpTagList(shot: Shot) {
        if (shot.tagList != null && shot.tagList!!.size > 0) {
            val layoutManager = FlexboxLayoutManager(this)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START
            shotTags.layoutManager = layoutManager
            mTagListAdapter = TagListAdapter(this, shot.tagList!!)
            shotTags.adapter = mTagListAdapter
        }
    }

    private fun initMathDataForImageAspectNewStyle() {
        //get status bar height
        val resourceId  = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusbarheight = resources.getDimensionPixelSize(resourceId)
        }

        yvalue =  emptyFourthreeView.measuredHeight.toFloat()
        scrolldiff = yvalue-zvalue
    }

    private fun manageImageAspectNewStyle(vRatio:Float) {
        /**
         * Vertical Transition effect on ShotImage
         */
        val transitionY = (vRatio + scrolldiff * (vRatio - 1) )-1//+ statusbarheight.toFloat()
        //picture.setOffset(transitionY)
        imgScrim.alpha=(vRatio*-0.7f)+0.7f
        Timber.d("b2oba: "+imgScrim.alpha)
    }

    private fun initMathDataForImageAspectOdStyle() {
        //ini math data
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        val screenHeightInDp = convertPixelsToDp(screenHeight.toFloat(), this@ShotDetailActivity)
        val emptyFourthreeViewHeight = emptyFourthreeView.measuredHeight
        val emptyFourthreeViewWidth = emptyFourthreeView.measuredWidth
        val emptyFourthreeViewHeightInDP = convertPixelsToDp(emptyFourthreeViewHeight.toFloat(), this@ShotDetailActivity)
        val shotImageToolbarHeightinDP = convertPixelsToDp(resources.getDimension(R.dimen.shot_image_toolbar_height), this@ShotDetailActivity)
        val shotImageToolbarHeightinPX = resources.getDimension(R.dimen.shot_image_toolbar_height)
        val ratioBigShotScreen = emptyFourthreeViewHeightInDP / screenHeightInDp
        val ratioMiniShotScreen = shotImageToolbarHeightinDP / screenHeightInDp

        ratioBigShotMiniShot = ratioMiniShotScreen / ratioBigShotScreen
        differenceWidthBigShotMiniShot = emptyFourthreeViewWidth / 2 - shotImageToolbarHeightinPX * 1.25f / 2//keep FourThreeImageView ratio
        differenceHeightBigShotMiniShot = emptyFourthreeViewHeight / 2 - shotImageToolbarHeightinPX / 2
    }

    private fun manageImageAspectOldVersion(verticalOffset:Int, vRatio:Float, imageIntoolbarPaddingIng:Float) {
        /**
         * Horizontal Transition effect on ShotImage
         */
        val transitionX = vRatio + (differenceWidthBigShotMiniShot - differenceWidthBigShotMiniShot * vRatio)
        val transitionXWithPadding = transitionX - (vRatio + -imageIntoolbarPaddingIng * (vRatio - 1))
        picture.x = transitionXWithPadding
        /**
         * Vertical Transition effect on ShotImage
         */
        val transitionY = vRatio + differenceHeightBigShotMiniShot * (vRatio - 1) + statusbarheight.toFloat()
        val transitionYWithPadding = transitionY - (vRatio + imageIntoolbarPaddingIng * (vRatio - 1))
        picture.y = transitionYWithPadding
        /**
         * scale effect effect on ShotImage
         */
        val scale = vRatio + differenceHeightBigShotMiniShot * (vRatio - 1)
        picture.scaleX = scale
        picture.scaleY = scale

        //Mnage shadow elevation according to scrol offset
        if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
            // Collapse
            picture.outlineProvider = ViewOutlineProvider.PADDED_BOUNDS
        } else if (verticalOffset == 0) {
            // Expanded
            picture.outlineProvider = null
        } else {
            // Somewhere in between
            picture.outlineProvider = null
        }

        customStatusBarbackground.alpha = vRatio
        if (isLightStatusBar) {
            // blackBackArrow.alpha = vRatio
            if (vRatio < 0.50) {
                MyColorUtils.clearLightStatusBar(mView)
            } else {
                MyColorUtils.setLightStatusBar(mView)
            }
        }
    }
}
