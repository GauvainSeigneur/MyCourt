package seigneur.gauvain.mycourt.ui.user

import android.app.Application
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.appcompat.widget.Toolbar
import android.text.Html
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

import java.util.ArrayList

import javax.inject.Inject

import butterknife.BindView
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.User
import seigneur.gauvain.mycourt.ui.base.BaseFragment
import timber.log.Timber

import seigneur.gauvain.mycourt.utils.MathUtils.convertPixelsToDp

/**
 * Created by gse on 22/11/2017.
 */
class UserFragment : BaseFragment() {

    @Inject
    lateinit var mApplication: Application

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mUserViewModel: UserViewModel by lazy {
         ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
    }

    @BindView(R.id.name)
    lateinit var name: TextView

    @BindView(R.id.login)
    lateinit var login: TextView

    @BindView(R.id.avatar)
    lateinit var avatar: ImageView

    @BindView(R.id.label_pro)
    lateinit var proLabel: ImageView

    @BindView(R.id.bio)
    lateinit var bio: TextView

    @BindView(R.id.app_bar)
    lateinit var appBarLayout: AppBarLayout

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.other_user_info_app_bar_layout)
    lateinit var userTextInfoInAppbar: LinearLayout

    @BindView(R.id.location)
    lateinit var userLocation: TextView

    @BindView(R.id.followers)
    lateinit var userFollowers: TextView

    @BindView(R.id.rv_user_links)
    lateinit var mUserLinksList: androidx.recyclerview.widget.RecyclerView

    private var mUserLinksAdapter: UserLinksAdapter? = null

    private var screenWidth: Int = 0

    override val fragmentLayout: Int
        get() = R.layout.fragment_user

    /*
    *********************************************************************************
    * Math & utils
    *********************************************************************************/
    /**
     * add to appbar a listener for scroll change in order to provide some
     * nice animation on scroll
     */
    private val appBarOffsetListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        val vTotalScrollRange = appBarLayout.totalScrollRange
        val vRatio = (vTotalScrollRange.toFloat() + verticalOffset) / vTotalScrollRange

        val avatarSize = avatar.width.toFloat()
        val avatarBigSize = resources.getDimension(R.dimen.avatar_bounds)
        val avatarMiniSize = resources.getDimension(R.dimen.avatar_mini)

        val ratioAvatarSizes = avatarMiniSize / avatarBigSize
        val diffBigMiniAvatarSize = avatarBigSize - avatarMiniSize

        val avatarMiniPaddingTopBottom = resources.getDimension(R.dimen.avatar_mini_padding_top_bottom)
        val avatarExpandTopMargin = resources.getDimension(R.dimen.avatar_big_margin_top)
        val avatarLeftMarginOnCollapsed = resources.getDimension(R.dimen.padding_16)
        val nameExpandTopMargin = resources.getDimension(R.dimen.user_name_expand_top_margin)
        val nameMargingLeftOnCollapsed = resources.getDimension(R.dimen.user_name_expand_left_margin)

        val nameWidth = name.width
        val nameHeight = name.height
        val diffToolbarHeightNameheight = toolbar.height - nameHeight

        /**
         * Horizontal Transition effect
         */
        /**
         * Horizontal Transition effect
         */
        val transitionX = vRatio * (screenWidth / 2 - avatarBigSize / 2) - (diffBigMiniAvatarSize / 2 - avatarLeftMarginOnCollapsed - (diffBigMiniAvatarSize / 2 - avatarLeftMarginOnCollapsed) * vRatio)
        avatar.x = transitionX

        val transitionXTitle = vRatio * (screenWidth / 2 - nameWidth / 2) + (nameMargingLeftOnCollapsed - nameMargingLeftOnCollapsed * vRatio)
        avatar.x = transitionX
        name.x = transitionXTitle
        /*float transitionXTitle = (vRatio - ((nameWidth/2)-((nameWidth/2)*vRatio))) +
                            (((nameWidth/4)+nameMargingLeftOnCollapsed)-(((nameWidth/4)+nameMargingLeftOnCollapsed)*vRatio));
                    name.setX(transitionXTitle);*/

        /**
         * scale effect effect on Avatar
         */
        /**
         * scale effect effect on Avatar
         */
        val scale = vRatio + (ratioAvatarSizes - ratioAvatarSizes * vRatio)
        avatar.scaleX = scale
        avatar.scaleY = scale

        /**
         * Vertical Transition effect
         */
        /**
         * Vertical Transition effect
         */
        val transitionY = ((vRatio * avatarExpandTopMargin//Manage padding when appBar is expanded
                - (diffBigMiniAvatarSize / 2 - diffBigMiniAvatarSize / 2 * vRatio)) //Manage size diff
                + (avatarMiniPaddingTopBottom - avatarMiniPaddingTopBottom * vRatio)) //Manage padding when appBar is collapsed
        avatar.y = transitionY

        val transitionNameY = (vRatio * nameExpandTopMargin//Manage padding when appBar is expanded
                + (diffToolbarHeightNameheight / 2 - diffToolbarHeightNameheight / 2 * vRatio)) //Manage padding when appBar is collapsed
        userTextInfoInAppbar.y = transitionNameY
        //name.setY(transitionNameY);
        userLocation.alpha = vRatio
        userFollowers.alpha = vRatio
    }

    /*
    ************************************************************************************
    *  Fragment lifecycle
    ************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        //provide ViewModel
        //fetch user if is not already fetched - todo make api prior
        mUserViewModel.init()
    }

    override fun onCreateView(inRootView: View, inSavedInstanceState: Bundle?) {
        //(activity as MainActivity).mTopAppBar.isSelected = false //todo - pass it to viewmodel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //listen livedata
        subscribeToLiveData(mUserViewModel)
        initMathData()
        appBarLayout.addOnOffsetChangedListener(appBarOffsetListener)
        appBarLayout.setExpanded(true)

    }

    /*
    ************************************************************************************
    * VIEWMODEL SUBSCRIPTION
    ************************************************************************************/
    private fun subscribeToLiveData(viewModel: UserViewModel) {
        viewModel.user
                .observe(
                        this,
                        Observer { user ->
                            if (user != null) {
                                setUpUserAccountInfo(user)
                                setUserPicture(user)
                                showUserLinks(user)
                            } else {
                                showNoUserFoundView(true)
                            }
                        }
                )

    }

    /*
    ************************************************************************************
    * UI methods
    ************************************************************************************/
    fun showNoConnectionView(visible: Boolean) {
        if (visible) {
            Toast.makeText(activity, "no connection", Toast.LENGTH_SHORT).show()
        }
    }

    fun setUpUserAccountInfo(user: User?) {
        name.text = user?.name
        login.text = user?.login
        userLocation.text = user?.location
        if (user!!.followers_count > 0)
            userFollowers.text = user.followers_count.toString() + " followers"
        else
            userFollowers.text = "no follower"
        if (!user.bio.isEmpty()) {
            bio.text = Html.fromHtml(user.bio)
        } else
            bio.text = "You didn't defined your bio"
        if (user.isPro)
            proLabel.visibility = View.VISIBLE
    }


    fun setUserPicture(user: User?) {
        Glide.with(mApplication)
                .load(Uri.parse(user?.avatar_url))
                .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.avatar_place_holder)
                        .error(R.drawable.avatar_place_holder)
                )
                .into(avatar)
    }

    fun showNoTeamsView(visible: Boolean) {}

    private fun showUserLinks(user: User?) {
        //todo - reactivate
        /*val userLinks = ArrayList<List<String>>()
        userLinks.add(mapToList(user!!.links))
        //mUserLinksList.setLayoutManager(new LinearLayoutManager(getContext()));
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        mUserLinksList.layoutManager = layoutManager
        mUserLinksAdapter = UserLinksAdapter(context!!, user.links)
        mUserLinksList.adapter = mUserLinksAdapter*/
    }


    fun showNoLinksView(visible: Boolean) {

    }


    fun showNoUserFoundView(visible: Boolean) {
        Toast.makeText(mApplication, "no user available", Toast.LENGTH_SHORT).show()
    }

    /**
     * init mathematics data for scroll animation
     * init these only when view was created in order to avoid some weird behavior on first opening
     */
    fun initMathData() {
        //ini math data
        val display = activity!!.windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        val screenHeightInDp = convertPixelsToDp(screenHeight.toFloat(), activity!!)
        screenWidth = displayMetrics.widthPixels
        val screenWidthDP = convertPixelsToDp(screenWidth.toFloat(), activity!!)
    }

    companion object {

        /**
         * Transform a key/value map to a a list of string using key
         * for links
         * @param env - map fetched
         * @return - list of links
         */
        fun mapToList(env: Map<String, String>): List<String> {
            val result = ArrayList<String>()
            for ((key) in env)
                result.add(key /*+ " " + entry.getValue()*/)
            return result
        }
    }

}
