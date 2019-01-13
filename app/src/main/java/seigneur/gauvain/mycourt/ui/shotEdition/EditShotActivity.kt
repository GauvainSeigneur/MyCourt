package seigneur.gauvain.mycourt.ui.shotEdition

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import com.google.android.material.textfield.TextInputEditText
import androidx.core.app.ActivityCompat
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat.canScrollVertically
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.yalantis.ucrop.UCrop

import java.io.File
import java.util.ArrayList

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Optional
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.android.AndroidInjection
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.FilePickerUtils
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Attachment
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.ui.shotEdition.attachmentList.AttachmentItemCallback
import seigneur.gauvain.mycourt.ui.shotEdition.attachmentList.AttachmentsAdapter
import seigneur.gauvain.mycourt.ui.shotEdition.attachmentList.UnScrollableLayoutManager
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.image.ImagePicker
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView
import seigneur.gauvain.mycourt.utils.FileUtils
import seigneur.gauvain.mycourt.utils.HttpUtils
import seigneur.gauvain.mycourt.utils.image.ImageUtils
import seigneur.gauvain.mycourt.utils.MyTextUtils
import timber.log.Timber

class EditShotActivity : BaseActivity() , AttachmentItemCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mApplication: Application

    private val  mShotEditionViewModel: ShotEditionViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ShotEditionViewModel::class.java)
    }

    @BindView(R.id.toolbar)
    lateinit var mToolbar: Toolbar

    @BindView(R.id.main)
    lateinit var layoutMain: androidx.coordinatorlayout.widget.CoordinatorLayout

    @BindView(R.id.shot_title_edt)
    lateinit var mShotTitleEditor: TextInputEditText

    @BindView(R.id.shot_tag_edt)
    lateinit var mTagEditor: TextInputEditText

    @BindView(R.id.shot_description_edt)
    lateinit var mShotDescriptionEditor: TextInputEditText

    @BindView(R.id.cropped_img_preview)
    lateinit var croppedImagePreview: FourThreeImageView

    @BindView(R.id.scroll_view)
    lateinit var mNestedScrollView: NestedScrollView

    @BindView(R.id.bs_publish)
    lateinit var mBSPublish: FrameLayout

    @BindView(R.id.btn_store)
    lateinit var storeBtn: FrameLayout

    @BindView(R.id.btn_publish)
    lateinit var publishBtn : FrameLayout

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
        setContentView(R.layout.activity_edit_shot)
        ButterKnife.bind(this)
        //provide Dependencies
        AndroidInjection.inject(this)
        //set up listeners and behaviors
        setUpEditorListener()
        setUpBottomSheet()
        setUpScrollListener()
        initAttachmentList()
        //Init ViewModel
        mShotEditionViewModel.init()
        //Subscribe to ViewModel data and event
        subscribeToLiveData(mShotEditionViewModel)
        subscribeToSingleEvent(mShotEditionViewModel)
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
            if (requestCode == Constants.PICK_IMAGE_REQUEST) {
                if (data != null) {
                    val shotIMG = ArrayList<String>()
                    shotIMG.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                    mShotEditionViewModel.mPickedFileUri = Uri.parse(shotIMG[0])
                    val shotFileName = FileUtils.getFileName(Uri.parse(shotIMG[0]))
                    val shotFileNameWithoutExtension = shotFileName.substringBefore(".",shotFileName)
                    mShotEditionViewModel.mPickedFileName = shotFileNameWithoutExtension
                    mShotEditionViewModel.mPickedFileMymeType = FileUtils.getMimeType((shotIMG[0]))
                    mShotEditionViewModel.mPickedImageDimens = FileUtils.getImageFilePixelSize(Uri.parse(shotIMG[0]))
                    //notify viewModel to call Ucrop command
                    mShotEditionViewModel.onImagePicked()
                }

            } else if (requestCode == UCrop.REQUEST_CROP) {
                mShotEditionViewModel.onImageCropped(UCrop.getOutput(data!!)!!)
            } else if (requestCode == Constants.PICK_ATTACHMENT_REQUEST) {
                if (data != null) {
                    val attachmentPaths=ArrayList<String>()
                    attachmentPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                    if (attachmentPaths.isNotEmpty()) {
                        Timber.d("SELECTED MEDIA"+ attachmentPaths[0])
                        val fileName = FileUtils.getFileName(Uri.parse(attachmentPaths[0]))
                        val fileNameWithoutExtension = fileName.substringBefore(".",fileName)
                        val attachment=Attachment(-1L,    //id to be -1 for new attachment
                                "",
                                attachmentPaths[0],
                                FileUtils.getMimeType((attachmentPaths[0])),
                                fileNameWithoutExtension)
                        val f = File(attachmentPaths[0])
                        mShotEditionViewModel.onAttachmentAdded(attachment)
                    }
                }

            }
        } else {
            Timber.d("SELECTED MEDIA is null")
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

        viewModel.getCroppedImageUri().observe(this, Observer<Uri> {
            this.displayShotImagePreview(it.toString())
            Timber.d("Uri change :$it")
        })

        viewModel.title.observe(this, Observer<String> {
            Timber.d("title change:$it")
        })

        viewModel.description.observe(this,  Observer<String> { Timber.d("desc change:$it") })

        viewModel.tags.observe(this,  Observer<ArrayList<String>> { Timber.d("tags change:$it") })

        viewModel.attachmentsTobeUploaded.observe(this,  Observer<ArrayList<Attachment>> {
            updateAttachmentList(it)
        })

        viewModel.isReadyToPubish.observe(this,  Observer<Boolean?> {
            Timber.d("is ready to publish: $it")
            activePublishBottomSheet(it)
        })
    }

    private fun subscribeToSingleEvent(viewModel: ShotEditionViewModel) {
        viewModel.setUpUiCmd.observe(
                this, Observer { it ->
            setUpEditionUI(it)
        }
        )

        viewModel.mPickShotCmd.observe(this, Observer {
            openImagePicker()
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
                Observer { Toast.makeText(this, "oops :" + it, Toast.LENGTH_SHORT).show() })

        viewModel.mCheckPerm.observe(this,
                Observer { checkPermissionExtStorage() })

        viewModel.mRequestPermCmd.observe(this,
                Observer {  requestPermission() })

        viewModel.onPublishSucceed.observe(this,
                Observer {
                    Toast.makeText(mApplication, "Publis suceed", Toast.LENGTH_SHORT)
                    finishAfterTransition()
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
    * Private
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

    private fun notifyPostSaved() {
        Toast.makeText(this, "ShotDraft saved in memory: ", Toast.LENGTH_SHORT).show()
        finishAfterTransition()
    }

    private fun openImagePicker() {
        FilePickerBuilder.instance.setMaxCount(1)
                .setActivityTheme(R.style.LibAppTheme)
                .pickPhoto(this, Constants.PICK_IMAGE_REQUEST)
    }

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

    private fun openConfirmMenu() {}

    private fun stopActivity() {
        finish()
    }

    private fun setUpEditionUI(draft: Draft) {
        //set toolbar title
        if (draft.typeOfDraft == Constants.EDIT_MODE_NEW_SHOT) {
            mToolbar.title = "Create a shot"
        } else {
            mToolbar.title = "Edit a shot"
        }

        //get title from draft object
        mShotTitleEditor.setText(draft.shot.title)

        //get description from draft objects
        val description = EditUtils.getDescription(draft)
        if (description != null && !description.isEmpty())
            mShotDescriptionEditor.setText(MyTextUtils.noTrailingwhiteLines(description))

        //get tags from draft objects
        mTagEditor.setText(EditUtils.getTagList(draft))

        draft.shot.attachment?.let {
            updateAttachmentList(ArrayList(draft.shot.attachment))
            for (i in ArrayList(draft.shot.attachment)) {
                mShotEditionViewModel.onAttachmentAdded(i)
            }
        }
        //get image from draft object
        if (draft.typeOfDraft == Constants.EDIT_MODE_UPDATE_SHOT) {
            displayShotImagePreview(draft.shot.imageNormal)
        } else {
            displayShotImagePreview(draft.imageUri)
        }

    }

    private fun displayShotImagePreview(uriImageCropped: String?) {
        if (uriImageCropped != null) {
            Glide.with(mApplication)
                    .asBitmap()
                    .load(Uri.parse(uriImageCropped))
                    .apply(RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .error(R.drawable.ic_my_shot_black_24dp)
                            // .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                            Toast.makeText(this@EditShotActivity, "error loading image", Toast.LENGTH_SHORT).show()
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            return false
                        }
                    })
                    .into(croppedImagePreview)
        } else {
            croppedImagePreview.setImageResource(R.drawable.add_image_illustration)
        }
    }

    private fun showMessageEmptyTitle() {
        Toast.makeText(this, "please define a title", Toast.LENGTH_SHORT).show()
    }

    private fun setUpEditorListener() {
        //Listen EditText
        mShotTitleEditor.addTextChangedListener(titleWatcher)
        mTagEditor.addTextChangedListener(tagWatcher)
        mShotDescriptionEditor.addTextChangedListener(descWatcher)
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

    private fun updateAttachmentList(atatchments: ArrayList<Attachment>) {
        attachments.clear() //clear list
        for (i in atatchments.indices) {
            //repopulate list
            attachments.add(atatchments[i])
        }
        mAttachmentsAdapter.updateRv()
    }

    private fun setUpScrollListener() {
        mNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener {
            v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                Timber.i("Scroll DOWN")
                mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
            }
            if (scrollY < oldScrollY) {
                Timber.i( "Scroll UP")
                mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            if (scrollY == 0) {
                Timber.i("TOP SCROLL")
                mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                Timber.i("BOTTOM SCROLL")
                mBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        })
    }

    private fun activePublishBottomSheet(activate:Boolean?) {
        if (activate==true) {
            publishBtn.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorAccent))
            storeBtn.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            mBSPublish.foreground = ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent))
        } else {
            publishBtn.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryLight))
            storeBtn.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryLight))
           //mBSPublish.foreground = ColorDrawable(ContextCompat.getColor(this, R.color.colorError))
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

    /*
    *********************************************************************************************
    * TEXTWATCHER
    *********************************************************************************************/
    private val titleWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { mShotEditionViewModel.onTitleChanged(s.toString()) }
        override fun afterTextChanged(s: Editable) {}
    }

    private val descWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { mShotEditionViewModel.onDescriptionChanged(s.toString()) }
        override fun afterTextChanged(s: Editable) {}
    }

    private val tagWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {  manageTagEdition(s)}
        override fun afterTextChanged(s: Editable) {}
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
}
