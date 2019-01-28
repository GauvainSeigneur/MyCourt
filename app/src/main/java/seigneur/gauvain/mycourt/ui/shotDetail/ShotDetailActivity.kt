package seigneur.gauvain.mycourt.ui.shotDetail

import android.app.ActionBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.Toolbar
import android.text.Html
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import javax.inject.Inject
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.android.AndroidInjection
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.shotDetail.attachments.AttachmentGridAdapter
import seigneur.gauvain.mycourt.ui.shotDetail.attachments.AttachmentListAdapter
import seigneur.gauvain.mycourt.ui.shotEdition.EditShotActivity
import seigneur.gauvain.mycourt.ui.shotEdition.attachmentList.AttachmentsAdapter
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView
import seigneur.gauvain.mycourt.utils.image.ImageUtils
import seigneur.gauvain.mycourt.utils.MyColorUtils
import seigneur.gauvain.mycourt.utils.MyTextUtils
import timber.log.Timber
import java.text.SimpleDateFormat

class ShotDetailActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mShotDetailViewModel: ShotDetailViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ShotDetailViewModel::class.java)
    }

    @BindView(R.id.parent)
    lateinit var mParentLayout: CoordinatorLayout

    @BindView(R.id.fake_app_bar)
    lateinit var mFakeAppBar: LinearLayout

    @BindView(R.id.app_bar)
    lateinit var appBarLayout: AppBarLayout

    @BindView(R.id.toolbar)
    lateinit var mToolbar: Toolbar

    @BindView(R.id.back_arrow)
    lateinit var backArrow: ImageView

    @BindView(R.id.dummy_fourthree_view)
    lateinit var emptyFourthreeView: FourThreeImageView

    @BindView(R.id.image_scrim)
    lateinit var imgScrim: View

    @BindView(R.id.shot_image)
    lateinit var picture: FourThreeImageView

    @BindView(R.id.shot_title)
    lateinit var shotTitle: TextView

    @BindView(R.id.shot_description)
    lateinit var shotDescription: TextView

    @BindView(R.id.tagGroup)
    lateinit var mTagGrpoup: ChipGroup

    @BindView(R.id.detail_attachment_layout)
    lateinit var mAttachmentLayout :LinearLayout

    @BindView(R.id.attachment_preview_title)
    lateinit var mattachmentTitle :TextView

    @BindView(R.id.rv_attachment_preview)
    lateinit var mAttachmentList: RecyclerView

    lateinit var mAttachmentListAdapter: AttachmentListAdapter

    @BindView(R.id.shot_update_date)
    lateinit var shotUpdate: TextView

    @BindView(R.id.fab)
    lateinit var fab: FloatingActionButton

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

    private var appBarOffsetListener: AppBarLayout.OnOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        val vTotalScrollRange = appBarLayout.totalScrollRange
        val vRatio = (vTotalScrollRange.toFloat() + verticalOffset) / vTotalScrollRange
        manageImageAspectNewStyle(vRatio)
    }

    /*
    *********************************************************************************************
    * LIFECYCLE
    *********************************************************************************************/
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

    @Optional
    @OnClick(R.id.fab)
    fun goToEdition() {
        mShotDetailViewModel.onEditClicked()
    }

    @Optional
    @OnClick(R.id.back_arrow)
    fun goToPrev() {
        finishAfterTransition()
    }

    /*
    *********************************************************************************************
    * LIVEDATA
    *********************************************************************************************/
    private fun subscribeToLiveData(viewModel: ShotDetailViewModel) {
        viewModel.shot
                .observe(
                        this,
                        Observer<Shot?> {
                            shot -> setUpShotImage(shot)
                        }

                )
    }

    private fun subscribeToSingleEvents(viewModel: ShotDetailViewModel) {
        // The activity observes the navigation commands in the ViewModel
        viewModel.editClickedEvent.observe(this, Observer { goToShotEdition() })

    }

    /*
    *********************************************************************************************
    * PRIVATE METHODS
    *********************************************************************************************/
    private fun goToShotEdition() {
        val intent = Intent(this, EditShotActivity::class.java)
        startActivity(intent)
    }

    private fun setUpShotImage(shot: Shot?) {
        loadShotImage(true, shot)
    }

    private fun loadShotImage(isTransactionPostponed: Boolean, shot: Shot?) {
        if (isTransactionPostponed)
            postponeEnterTransition()
        /**
         * AS shot image is loaded from Glide ressource, put listener to define when to start startPostponedEnterTransition
         */
        Glide
                .with(this)
                .load(Uri.parse(shot!!.imageUrl))
                .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .error(R.drawable.ic_my_shot_black_24dp)
                )
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
                            //TODO - singleLive event and try reload it
                            setUpShotInfoView(shot, true, resource)
                        }
                        return false
                    }
                })
                .into(picture)
    }

    private fun setUpShotInfoView(shot: Shot?, isImageReady: Boolean, drawable: Drawable?) {
        startPostponedEnterTransition()
        showPaletteShot(false)
        if (isImageReady && drawable != null)
            adaptColorToShot(drawable)
        initImageScrollBehavior()
        setUpShotInfo(shot)
    }

    private fun adaptColorToShot(resource: Drawable) {
        val bitmap = ImageUtils.drawableToBitmap(resource)
        recolorStatusBar(bitmap)
        recolorShadowColor(bitmap)
    }

    private fun recolorStatusBar(bitmap: Bitmap) {
        val twentyFourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                24f, this.resources.displayMetrics).toInt()
        // Bitmap bitmap = BitmapFactory.decodeResource(drawable);
        androidx.palette.graphics.Palette.from(bitmap)
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
                        window.statusBarColor = dominantSwatchSwatch.rgb
                        //customStatusBarbackground.startAnimation(fadeIn)
                        //customStatusBarbackground.setBackgroundColor(dominantSwatchSwatch.rgb)
                    }

                    if (!MyColorUtils.isDark(dominantColor)) {
                        isLightStatusBar = true
                        //blackBackArrow.alpha = 1.0f
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            MyColorUtils.setLightStatusBar(mView)
                        }
                       // backArrow.tint
                        val darkerGrey = ContextCompat.getColor(this, R.color.colorPrimaryLight)
                        ImageViewCompat.setImageTintList(backArrow,
                                ColorStateList.valueOf(darkerGrey))
                    } else {
                    }
                }
    }

    private fun recolorShadowColor(bitmap: Bitmap) {
        val twentyFourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                24f, this.resources.displayMetrics).toInt()
        androidx.palette.graphics.Palette.from(bitmap)
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

    private fun showErrorView(visible: Boolean) {
        if (visible)
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
    }


    private fun showPaletteShot(isVisible: Boolean) {
        //if isVisible show layout and show palette color!
    }

    private fun setUpShotInfo(shot: Shot?) {
        shotTitle.text = shot!!.title
        shotDescription.text = shotDescription(shot)
        val dateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss")
        val publishDate = dateFormat.format( shot.publishDate)
        shotUpdate.text = "Published: "+publishDate
        setUpTagList(shot)
        //attachments
        setUpAttachmentList(shot)
    }

    private fun setUpAttachmentList(shot: Shot?) {
        shot?.attachment?.let {
            if (shot.attachment!!.isNotEmpty()) {
                mAttachmentLayout.visibility = View.VISIBLE
                val count = shot.attachment!!.size
                val attachmentTitle = resources.getQuantityString(R.plurals.title_attachment, count, count)
                mattachmentTitle.text =  attachmentTitle

                //mGridAdapter = AttachmentGridAdapter(this, shot.attachment!!)
                //mAttachments.adapter =mGridAdapter
                val layoutManager =LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                mAttachmentListAdapter = AttachmentListAdapter(this, shot.attachment!!)
                mAttachmentList.layoutManager = layoutManager
                mAttachmentList.adapter = mAttachmentListAdapter
            }

        }
    }

    private fun showEditionResult(result: Int) {
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
        //https://stackoverflow.com/questions/50494502/how-can-i-add-the-new-android-chips-dynamically-in-android
        if (shot.tagList != null && shot.tagList!!.size > 0) {
            for (i in shot.tagList!!) {
                val chip = Chip(mTagGrpoup.context)
                chip.text=i
                // necessary to get single selection working
                //chip.isClickable = true
                //chip.isCheckable = true
                chip.setTextAppearance(R.style.chipTextAppearance)
                //chip.chipBackgroundColor = this.resources.getColorStateList(R.drawable.chip_state_list)
                chip.setChipBackgroundColorResource(R.color.colorPrimaryLight)
                chip.setChipStrokeColorResource(R.color.colorPrimaryLight)
                mTagGrpoup.addView(chip)
            }

        }
    }

    private fun initImageScrollBehavior() {
        appBarLayout.addOnOffsetChangedListener(appBarOffsetListener)
    }

    private fun manageImageAspectNewStyle(vRatio:Float) {
        imgScrim.alpha=(vRatio*-0.7f)+0.7f
    }

}
