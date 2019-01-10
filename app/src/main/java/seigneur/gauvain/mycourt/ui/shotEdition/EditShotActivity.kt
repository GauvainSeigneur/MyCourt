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
import android.net.Uri
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
        //Init ViewModel
        mShotEditionViewModel.init()
        //Subscribe to ViewModel data and event
        subscribeToLiveData(mShotEditionViewModel)
        subscribeToSingleEvent(mShotEditionViewModel)
        testRvAttachment()
    }

    private fun testRvAttachment() {
        if (mRvAttachments.layoutManager==null && mRvAttachments.adapter==null) {
            mGridLayoutManager = UnScrollableLayoutManager(this, 5)
            mRvAttachments.layoutManager =  mGridLayoutManager
            mRvAttachments.adapter = mAttachmentsAdapter
            (mRvAttachments.layoutManager as UnScrollableLayoutManager).disableScrolling() //disable scroll
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST) {
                mShotEditionViewModel.imagePickedUriSource = data?.data
                mShotEditionViewModel.imagePickedFileName = ImagePicker.getPickedImageName(this, mShotEditionViewModel.imagePickedUriSource!!)
                mShotEditionViewModel.imagePickedFormat = ImageUtils.getImageExtension(this, mShotEditionViewModel.imagePickedUriSource!!)
                mShotEditionViewModel.imageSize = ImageUtils.imagePickedWidthHeight(this, mShotEditionViewModel.imagePickedUriSource!!, 0)
                mShotEditionViewModel.onImagePicked()
            } else if (requestCode == UCrop.REQUEST_CROP) {
                mShotEditionViewModel.onImageCropped(UCrop.getOutput(data!!)!!)
            }
            else if (requestCode == Constants.PICK_ATTACHMENT_REQUEST) {
                Timber.d("PICK_ATTACHMENT_REQUEST called")
                val attachmentUri = data?.data
                val attachmentFormat = ImageUtils.getImageExtension(this, attachmentUri!!)
                val attachmentFileName = ImagePicker.getPickedImageName(this, attachmentUri)
                val attachmentFileUri:String = ImagePicker.getImageFilePathFromContentUri(this, attachmentFileName).toString()
                /*val attachment=Attachment(-1L,    //id to be -1 for new attachment
                        "",
                        attachmentUri.toString(),
                        attachmentFormat!!,
                        attachmentFileName)
                mShotEditionViewModel.onAttachmentAdded(attachment)*/



                val uri  = data.data
                Timber.d("uri: $uri")
                //val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                //val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                // Get the cursor
                val cursor = contentResolver.query(uri,
                        filePathColumn,
                        null,
                        null,
                        null)
                // Move to first row
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                var imgDecodableString:String?=""
                imgDecodableString = cursor.getString(columnIndex)
                cursor.close()

                Timber.d("imgDecodableString: $imgDecodableString")
                //val imgView = (ImageView) findViewById(R.id.groupPic);
                // Set the Image in ImageView after decoding the String
                //imgView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));

                val attachment=Attachment(-1L,    //id to be -1 for new attachment
                        "",
                        imgDecodableString,
                        attachmentFormat!!,
                        attachmentFileName)
                mShotEditionViewModel.onAttachmentAdded(attachment)


            }
        } else {
            if (resultCode == UCrop.RESULT_ERROR)
                mShotEditionViewModel.onPickCropError(resultCode)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
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
            this.displayShotImagePreview(it)
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
                this, Observer { setUpEditionUI(mShotEditionViewModel.getTempDraft()!!) }
        )

        viewModel.pickShotCommand.observe(this, Observer { openImagePicker() })

        viewModel.pickAttachmentCommand.observe(this, Observer { openAttachmentPicker() })

        viewModel.cropImageCmd.observe(this,
                 Observer {
                    goToUCropActivity(
                            mShotEditionViewModel.imagePickedFormat,
                            mShotEditionViewModel.imagePickedUriSource,
                            mShotEditionViewModel.imagePickedFileName,
                            mShotEditionViewModel.imageSize)
                })

        viewModel.pickCropImgErrorCmd.observe(this,
                Observer { Toast.makeText(this, "oops :" + it, Toast.LENGTH_SHORT).show() })

        viewModel.checkPerm.observe(this,
                Observer { checkPermissionExtStorage() })

        viewModel.requestPermCmd.observe(this,
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
    * INNER
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
        ImagePicker.pickImage(this, Constants.PICK_IMAGE_REQUEST)
    }

    private fun openAttachmentPicker() {
        ImagePicker.pickImage(this, Constants.PICK_ATTACHMENT_REQUEST)
    }

    private fun goToUCropActivity(imagePickedformat: String?,
                          imagePickedUriSource: Uri?,
                          imagePickedFileName: String?,
                          imageSize: IntArray?) {
        ImagePicker.goToUCropActivity(imagePickedformat,
                imagePickedUriSource!!,
                ImagePicker.getImageFilePathFromContentUri(this, imagePickedFileName!!),
                //Uri.fromFile(File(cacheDir, imagePickedFileName!!)),
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

        //get image from draft object
        if (draft.imageUri == null)
            croppedImagePreview.setImageResource(R.drawable.add_image_illustration)
        val imageCroppedUri :Uri? = EditUtils.getShotImageUrl(this, draft)
        if (imageCroppedUri!=null)
            mShotEditionViewModel.onImageCropped(imageCroppedUri)

        //get title from draft object
        mShotTitleEditor.setText(draft.shot.title)

        //get description from draft objects
        val description = EditUtils.getDescription(draft)
        if (description != null && !description.isEmpty())
            mShotDescriptionEditor.setText(MyTextUtils.noTrailingwhiteLines(description))

        //get tags from draft objects
        mTagEditor.setText(EditUtils.getTagList(draft))

        //get attachments from Draft object and send data to ViewModel to update UI
        draft.shot.attachment?.let {
            updateAttachmentList(ArrayList(draft.shot.attachment))
            for (i in ArrayList(draft.shot.attachment)) {
                mShotEditionViewModel.onAttachmentAdded(i)
            }
        }

    }

    private fun displayShotImagePreview(uriImageCropped: Uri?) {
        if (uriImageCropped != null) {
            Glide.with(mApplication)
                    .asBitmap()
                    .load(uriImageCropped)
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
