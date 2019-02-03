package seigneur.gauvain.mycourt.ui.about

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import javax.inject.Inject
import butterknife.BindView
import com.google.android.material.shape.*
import com.google.android.material.switchmaterial.SwitchMaterial
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.local.SharedPrefs
import seigneur.gauvain.mycourt.ui.base.BaseFragment
import seigneur.gauvain.mycourt.ui.main.MainActivity
import seigneur.gauvain.mycourt.ui.widget.FourThreeVideoView
import timber.log.Timber

/**
 * Created by gse on 22/11/2017.
 */
class AboutFragment : BaseFragment() {

    @Inject
    lateinit var mApplication: Application

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.button)
    lateinit var mButton : Button

    @BindView(R.id.video_player)
    lateinit var mVideoView : FourThreeVideoView

    @BindView(R.id.swicth)
    lateinit var mSwicth : SwitchMaterial

    @Inject
    lateinit var mSharedPrefs :SharedPrefs

    private val mAboutViewModel: AboutViewModel by lazy {
         ViewModelProviders.of(this, viewModelFactory).get(AboutViewModel::class.java)
    }

    /*
    ************************************************************************************
    *  Fragment lifecycle
    ************************************************************************************/
    override fun onCreateView(inRootView: View, inSavedInstanceState: Bundle?) {
        mAboutViewModel.init()

        //from : https://developers.googleblog.com/2018/12/building-shape-system-for-material.html#gpluscomments
        /*val bg = mButton.background as MaterialShapeDrawable?
        bg?.let {
            it.shapeAppearanceModel.apply {
                CutCornerTreatment(15f)
            }
        }*/

        val shapePathModel = ShapeAppearanceModel()
        shapePathModel.topLeftCorner = RoundedCornerTreatment(100f)
        shapePathModel.bottomRightCorner= RoundedCornerTreatment(100f/*radius corner in dp*/)
        shapePathModel.topRightCorner = CutCornerTreatment(100f)
        val leftRoundedMaterialShape = MaterialShapeDrawable(shapePathModel)
        mButton.background=leftRoundedMaterialShape


       /* mVideoView.setVideoPath("https://cdn.dribbble.com/users/1969947/videos/9961/__.mp4")
        mVideoView.start()
        mVideoView.setOnErrorListener(mOnErrorListener)
        mVideoView.setOnPreparedListener{
            //callback - video is ready to be played
            Timber.d("is prepared")
            it.isLooping =true //allow video to repeat

        }*/


            mSwicth.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            mSwicth.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    mSharedPrefs.putBoolean(SharedPrefs.kNightMode,true)
                    val intent = Intent(activity, MainActivity::class.java)
                    //intent.putExtra("colorIntentFromSetting", 4)
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    activity?.finish()
                    activity?.startActivity(intent)
                } else {
                    mSharedPrefs.putBoolean(SharedPrefs.kNightMode,false)
                    val intent = Intent(activity, MainActivity::class.java)
                    //intent.putExtra("colorIntentFromSetting", 4)
                    //add paramter
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    activity?.finish()
                    activity?.startActivity(intent)
                }
            }


    }


    private val mOnErrorListener = object : MediaPlayer.OnErrorListener {
       override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            // Your code goes here
           Timber.d("error loading video")
            return true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //listen LiveData
        subscribeToLiveData(mAboutViewModel)
    }

    override val fragmentLayout: Int
        get() = R.layout.fragment_about
    /*
    ************************************************************************************
    * VIEWMODEL SUBSCRIPTION
    ************************************************************************************/
    private fun subscribeToLiveData(viewModel: AboutViewModel) {
    }

}
