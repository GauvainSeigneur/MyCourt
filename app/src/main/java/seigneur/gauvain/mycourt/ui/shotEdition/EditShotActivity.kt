package seigneur.gauvain.mycourt.ui.shotEdition

import android.Manifest
import android.app.Activity
import android.app.Application
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import com.google.android.material.textfield.TextInputEditText
import androidx.core.app.ActivityCompat
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.widget.NestedScrollView
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


import com.yalantis.ucrop.UCrop

import java.io.File
import java.util.ArrayList

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.models.sort.SortingTypes
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Attachment
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.shotEdition.attachmentList.AttachmentItemCallback
import seigneur.gauvain.mycourt.ui.shotEdition.attachmentList.AttachmentsAdapter
import seigneur.gauvain.mycourt.ui.shotEdition.attachmentList.UnScrollableLayoutManager
import seigneur.gauvain.mycourt.utils.image.ImagePicker
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView
import seigneur.gauvain.mycourt.ui.widget.FourThreeVideoView
import seigneur.gauvain.mycourt.utils.*
import timber.log.Timber

class EditShotActivity : BaseActivity() , AttachmentItemCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mApplication: Application

    private val mShotEditionViewModel: ShotEditionViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ShotEditionViewModel::class.java)
    }

    @BindView(R.id.toolbar)
    lateinit var mToolbar: Toolbar

    @BindView(R.id.main)
    lateinit var layoutMain: CoordinatorLayout

    @BindView(R.id.shot_title_edt)
    lateinit var mShotTitleEditor: TextInputEditText

    @BindView(R.id.shot_tag_edt)
    lateinit var mTagEditor: TextInputEditText

    @BindView(R.id.shot_description_edt)
    lateinit var mShotDescriptionEditor: TextInputEditText

    @BindView(R.id.cropped_img_preview)
    lateinit var croppedImagePreview: FourThreeImageView

    @BindView(R.id.video_view)
    lateinit var mVideoView: FourThreeVideoView

    @BindView(R.id.edit_shot_container)
    lateinit var mEditionContainer: NestedScrollView

    @BindView(R.id.bs_publish)
    lateinit var mBSPublish: LinearLayout

    @BindView(R.id.btn_store)
    lateinit var storeBtn: Button

    @BindView(R.id.btn_publish)
    lateinit var publishBtn : Button

    private lateinit var mBottomSheetBehaviour : BottomSheetBehavior<View>

    //Attachments
    @BindView(R.id.rv_attachment)
    lateinit var mRvAttachments: RecyclerView
    lateinit var mGridLayoutManager: GridLayoutManager
    var attachments=ArrayList<Attachment>()
    private val mAttachmentsAdapter: AttachmentsAdapter by lazy {
        AttachmentsAdapter(attachments, this)
    }

    /*
    *********************************************************************************************
    * LIFECYCLE
    *********************************************************************************************/
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //provide Dependencies
        AndroidInjection.inject(this)
        //Init ViewModel before inflate views to perform first request
        mShotEditionViewModel.init()
        setContentView(R.layout.activity_edit_shot)
        ButterKnife.bind(this)
        //Subscribe to ViewModel data and event only when View is inflated
        subscribeToLiveData(mShotEditionViewModel)
        subscribeToSingleEvent(mShotEditionViewModel)
        //set up listeners and behaviors
        setUpEditorListener()
        setUpBottomSheet()
        setUpScrollListener()
        initAttachmentList()
    }

    /**
     * Callback received when a permissions request has been completed.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mShotEditionViewModel.onPermGranted()
            } else {
                mShotEditionViewModel.requestPerm()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.PICK_IMAGE_REQUEST ->
                    ImagePicker.onImagePicked(data, mShotEditionViewModel)
                UCrop.REQUEST_CROP ->
                    ImagePicker.onImageCropped(data, mShotEditionViewModel)
                Constants.PICK_ATTACHMENT_REQUEST ->
                    ImagePicker.onAttachmentPicked(data, mShotEditionViewModel)
                else -> Timber.d("SELECTED MEDIA is null")
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    @OnClick(R.id.btn_store)
    fun store() {
        mShotEditionViewModel.onStoreDraftClicked()
    }

    @Optional
    @OnClick(R.id.btn_publish)
    fun publish() {
        mShotEditionViewModel.onPublishClicked()
    }

    @Optional
    @OnClick(R.id.cropped_img_preview)
    fun shotPreviewClick() {
        mShotEditionViewModel.onImagePreviewClicked()
    }


    /*
    *********************************************************************************************
    * EVENT WHICH VIEW WILL SUBSCRIBE
    *********************************************************************************************/
    private fun subscribeToLiveData(viewModel: ShotEditionViewModel) {
        viewModel.getUserType.observe(this,  Observer<Int> {
            manageEditionOption(it)
        })

        viewModel.getFileToUploadUri().observe(this, Observer<Uri> {
            if (FileUtils.getMimeType(it.toString()).equals(Constants.MP4)) {
                //todo if is video
                mVideoView.setVideoPath(it.toString())
                mVideoView.start()
                mVideoView.setOnErrorListener(mOnErrorListener)
                mVideoView.setOnPreparedListener{
                    //callback - video is ready to be played
                    Timber.d("is prepared")
                    it.isLooping =true //allow video to repeat
                }
            } else {
                Timber.d("image uri $it")
                this.displayShotImagePreview(it.toString())
            }
        })

        viewModel.title.observe(this, Observer<String> { Timber.d("title change:$it") })

        viewModel.description.observe(this,  Observer<String> { Timber.d("desc change:$it") })

        viewModel.tags.observe(this,  Observer<ArrayList<String>> { Timber.d("tags change:$it") })

        viewModel.attachmentsTobeUploaded.observe(this,  Observer<ArrayList<Attachment>> {
            updateAttachmentList(it)
        })

        viewModel.isReadyToPublish.observe(this,  Observer<Boolean?> {
            activePublishBottomSheet(it)
        })
    }

    private fun subscribeToSingleEvent(viewModel: ShotEditionViewModel) {
        viewModel.setUpUiCmd.observe(
                this, Observer { it ->
            manageUiFromDraftInfo(it)
        })

        viewModel.mPickShotCmd.observe(this, Observer {
            FilePickerBuilder.instance
                    .setMaxCount(1)
                    .setActivityTheme(R.style.LibAppTheme)
                    .showGifs(true)
                    //.enableVideoPicker(true) //API doesn't allow to upload VIDEO YET
                    .showFolderView(true)
                    .pickPhoto(this, Constants.PICK_IMAGE_REQUEST)
        })

        viewModel.mPickAttachmentCmd.observe(this, Observer {
            openAttachmentPicker()
        })

        viewModel.mCropImgCmd.observe(this,
                 Observer {
                    goToUCropActivity(
                            mShotEditionViewModel.mPickedFileMymeType,
                            FileUtils.getContentUriFromFilePath(this,
                                    mShotEditionViewModel.mPickedFileUri.toString()),
                            Uri.fromFile( File(this.cacheDir, mShotEditionViewModel.mPickedFileName)),
                            mShotEditionViewModel.mPickedImageDimens)
                })

        viewModel.pickCropImgErrorCmd.observe(this,
                Observer { Toast.makeText(this, getString(R.string.error_picking), Toast.LENGTH_SHORT).show() })

        viewModel.mCheckPerm.observe(this,
                Observer { checkPermissionExtStorage() })

        viewModel.mRequestPermCmd.observe(this,
                Observer {  requestPermission() })

        viewModel.onPublishSucceed.observe(this,
                Observer {
                    Toast.makeText(mApplication, "Publish suceed", Toast.LENGTH_SHORT)
                    finishAfterTransition()
                })

        viewModel.notifyUserNotReadyCmd.observe(this, Observer {
            val snack = Snackbar.make(findViewById(R.id.main),
                "Oops, please set a title and image", Snackbar.LENGTH_SHORT)
            SnackbarHelper.configSnackbar(this, snack)
            snack.show()

        })
    }

    /*
    *********************************************************************************************
    * AttachmentItemCallback
    *********************************************************************************************/
    override fun onAddClicked() {
        mShotEditionViewModel.onAddAttachmentClicked()
}

    override fun onAttachmentClicked(position: Int) {
    }

    override fun onAttachmentDeleted(position: Int) {
        mShotEditionViewModel.onRemoveAttachment(position)
    }

    /*
    *********************************************************************************************
    * REQUEST PERMISSION TO READ AND WRITE ON EXTERNAL STORAGE
    *********************************************************************************************/
    private fun checkPermissionExtStorage() {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            mShotEditionViewModel.requestPerm()
        } else {
            mShotEditionViewModel.onPermGranted()
        }
    }

    private fun requestPermission() {
        requestPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_storage_rationale),
                Constants.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
    }

    /*
    *********************************************************************************************
    * PICKER RELATED METHODS
    *********************************************************************************************/
    private fun openAttachmentPicker() {
        //Manage multiple selection later
        FilePickerBuilder.instance.setMaxCount(1)
                .setActivityTheme(R.style.LibAppTheme)
                .pickPhoto(this, Constants.PICK_ATTACHMENT_REQUEST)
    }

    private fun goToUCropActivity(
            imagePickedformat: String?,
            source: Uri?,
            destination: Uri,
            imageSize: IntArray?) {
        ImagePicker.goToUCropActivity(
                imagePickedformat,
                source!!,
                destination,
                this,
                imageSize!!)
    }

    private fun showImageNotUpdatable() {
        Toast.makeText(this@EditShotActivity, "Shot can't be changed in edition mode", Toast.LENGTH_SHORT).show()
    }


    private val mOnErrorListener = object : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            // Your code goes here
            Timber.d("error loading video")
            return true
        }
    }

    /*
    *********************************************************************************************
    * EDITION UI RELATED FUNCTIONS
    *********************************************************************************************/
    /**
     * Manage editions options visibility in accordance to user type
     */
    private fun manageEditionOption(userType:Int) {
        when (userType) {
            Constants.USER_UPLOADER -> {
                mEditionContainer.visibility=View.VISIBLE
                mRvAttachments.visibility =View.GONE
                mBSPublish.visibility=View.VISIBLE
            }
            Constants.USER_PRO -> {
                mEditionContainer.visibility=View.VISIBLE
                mRvAttachments.visibility =View.VISIBLE
                mBSPublish.visibility=View.VISIBLE
            }
            else -> {
                mEditionContainer.visibility=View.GONE
                mBSPublish.visibility=View.GONE
            }
        }

    }

    /**
     * Manage UI in accordance to draft information defined in Draft
     */
    private fun manageUiFromDraftInfo(draft: Draft) {
        //set toolbar title
        if (draft.typeOfDraft == Constants.EDIT_MODE_NEW_SHOT) {
            mToolbar.title = getString(R.string.title_create_shot)
        } else {
            mToolbar.title = getString(R.string.title_edit)
        }
        //get title from draft object
        mShotTitleEditor.setText(draft.shot.title)
        //get description from draft objects
        val description = EditUtils.getDescription(draft)
        if (description != null && !description.isEmpty())
            mShotDescriptionEditor.setText(MyTextUtils.noTrailingwhiteLines(description))
        //get tags from draft objects
        mTagEditor.setText(EditUtils.getTagList(draft))
        //DISPLAY DEFAULT SHOT IMAGE FOR PUBLISHED SHOT AND NEW DRAFT
        //FOR DRAFT WITH STORED CROPPED IMAGE, WE GET IT FROM LIVEDATA
        if (draft.typeOfDraft == Constants.EDIT_MODE_UPDATE_SHOT) {
            displayShotImagePreview(draft.shot.imageHidpi) //high quality image displayed
        } else {
            if (draft.imageUri.isNullOrEmpty())
                displayShotImagePreview(null)
        }
    }

    private fun displayShotImagePreview(uriImageCropped: String?) {
       EditUtils.displayImage(uriImageCropped, this, croppedImagePreview)
    }

    private fun showMessageEmptyTitle() {
        Toast.makeText(this, "please define a title", Toast.LENGTH_SHORT).show()
    }

    private fun setUpBottomSheet() {
        mBottomSheetBehaviour = BottomSheetBehavior.from(mBSPublish)
        mBottomSheetBehaviour.state= BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetBehaviour.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    private fun setUpScrollListener() {
        mEditionContainer.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener {
            v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                Timber.i("Scroll DOWN")
                mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            if (scrollY < oldScrollY) {
                Timber.i( "Scroll UP")
                mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            if (scrollY == 0) {
                Timber.i("TOP SCROLL")
                mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

            }

            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                Timber.i("BOTTOM SCROLL")
                mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        })
    }

    private fun activePublishBottomSheet(activate:Boolean?) {}

    /*
    *********************************************************************************************
    * TEXT WATCHER AND EDITION LISTENER
    *********************************************************************************************/
    private fun setUpEditorListener() {
        //Listen EditText
        mShotTitleEditor.addTextChangedListener(getTextWatcher(mShotTitleEditor))
        mTagEditor.addTextChangedListener(getTextWatcher(mTagEditor))
        mShotDescriptionEditor.addTextChangedListener(getTextWatcher(mShotDescriptionEditor))
    }

    private fun getTextWatcher(editText: TextInputEditText): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                when(editText) {
                    mShotTitleEditor ->  mShotEditionViewModel.onTitleChanged(charSequence.toString())
                    mTagEditor ->  manageTagEdition(charSequence)
                    mShotDescriptionEditor -> mShotEditionViewModel.onDescriptionChanged(charSequence.toString())
                    else -> Timber.d("no action")
                }

            }
            override fun afterTextChanged(editable: Editable) {}
        }
    }

    private fun manageTagEdition(s: CharSequence) {
        val tagString = s.toString()
        val commas = s.toString().replace("[^,]".toRegex(), "").length
        Timber.d(commas.toString() + "")
        if (commas < 12) {
            mShotEditionViewModel.onTagChanged(tagString)
        } else {

        }
    }

    private fun initAttachmentList() {
        if (mRvAttachments.layoutManager==null && mRvAttachments.adapter==null) {
            mGridLayoutManager = UnScrollableLayoutManager(this, 5)
            mRvAttachments.layoutManager =  mGridLayoutManager
            mRvAttachments.adapter = mAttachmentsAdapter
            (mRvAttachments.layoutManager as UnScrollableLayoutManager).disableScrolling() //disable scroll
        }
    }

    private fun updateAttachmentList(newAttachmentList: ArrayList<Attachment>) {
        Timber.d("updateAttachmentList called")
        attachments.clear() //clear list
        for (i in newAttachmentList.indices) {
            //repopulate list
            attachments.add(newAttachmentList[i])
        }
        mAttachmentsAdapter.updateRv()
    }

}
