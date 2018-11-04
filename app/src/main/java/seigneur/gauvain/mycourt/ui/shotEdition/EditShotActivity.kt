package seigneur.gauvain.mycourt.ui.shotEdition

import android.Manifest
import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TextInputEditText
import android.support.v4.app.ActivityCompat
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.yalantis.ucrop.UCrop

import java.io.File
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.inject.Inject

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import dagger.android.AndroidInjection
import seigneur.gauvain.mycourt.R
import seigneur.gauvain.mycourt.data.model.Draft
import seigneur.gauvain.mycourt.data.model.Shot
import seigneur.gauvain.mycourt.ui.base.BaseActivity
import seigneur.gauvain.mycourt.utils.Constants
import seigneur.gauvain.mycourt.utils.image.ImagePicker
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView
import seigneur.gauvain.mycourt.utils.image.ImageUtils
import seigneur.gauvain.mycourt.utils.MyTextUtils
import timber.log.Timber

class EditShotActivity : BaseActivity() {

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
    lateinit var layoutMain: CoordinatorLayout

    @BindView(R.id.shot_title_edt)
    lateinit var mShotTitleEditor: TextInputEditText

    @BindView(R.id.shot_tag_edt)
    lateinit var mTagEditor: TextInputEditText

    @BindView(R.id.shot_description_edt)
    lateinit var mShotDescriptionEditor: TextInputEditText

    @BindView(R.id.cropped_img_preview)
    lateinit var croppedImagePreview: FourThreeImageView

    @BindView(R.id.btn_store)
    lateinit var storeBtn: Button

    /*
    *********************************************************************************************
    * TEXTWATCHER
    *********************************************************************************************/
    private val titleWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            mShotEditionViewModel.onTitleChanged(s.toString())
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private val tagWtacher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            //todo - must manage tag limit in ViewmODEL
            val tagString = s.toString()
            val commas = s.toString().replace("[^,]".toRegex(), "").length
            Timber.d(commas.toString() + "")
            if (commas < 12) {
                mShotEditionViewModel.onTagChanged(tagString)
            } else {

            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private val descriptionWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            mShotEditionViewModel.onDescriptionChanged(s.toString())
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_shot)
        ButterKnife.bind(this)
        //provide Dependencies
        AndroidInjection.inject(this)
        //Listen EditText
        mShotTitleEditor.addTextChangedListener(titleWatcher)
        mTagEditor.addTextChangedListener(tagWtacher)
        mShotDescriptionEditor.addTextChangedListener(descriptionWatcher)
        //Provide ViewModel
        mShotEditionViewModel.init()
        //Subscribe to ViewModel data and event
        subscribeToLiveData(mShotEditionViewModel)
        subscribeToSingleEvent(mShotEditionViewModel)

    }

    @OnClick(R.id.btn_store)
    fun store() {
        mShotEditionViewModel.onStoreDraftClicked()
    }

    @OnClick(R.id.btn_publish)
    fun publish() {
        mShotEditionViewModel.onPublishClicked()
    }

    /*
     *********************************************************************************************
     * EVENT WHICH VIEW WILL SUBSCRIBE
     *********************************************************************************************/
    private fun subscribeToLiveData(viewModel: ShotEditionViewModel) {
        viewModel.getCroppedImageUri().observe(this, Observer<Uri> { this.displayShotImagePreview(it) })

        viewModel.title.observe(this, Observer<String> {Timber.d("title change:$it") })

        viewModel.description.observe(this,  Observer<String> { Timber.d("desc change:$it") })

        viewModel.tags.observe(this,  Observer<ArrayList<String>> { Timber.d("tags change:$it") })
    }

    private fun subscribeToSingleEvent(viewModel: ShotEditionViewModel) {

        viewModel.setUpUiCmd.observe(
                this, Observer { setUpEditionUI(mShotEditionViewModel.getmTempDraft()!!) }
        )

        viewModel.pickShotCommand.observe(this, Observer { openImagePicker() })

        viewModel.cropImageCmd.observe(this,
                 Observer {
                    goToUCropActivity(
                            mShotEditionViewModel.imagePickedFormat,
                            mShotEditionViewModel.imagePickedUriSource,
                            mShotEditionViewModel.imagePickedFileName,
                            mShotEditionViewModel.imageSize)
                })

        viewModel.pickCropImgErrorCmd.observe(this,Observer<Int>
                { Toast.makeText(this, "oops :" + it, Toast.LENGTH_SHORT).show() })

        viewModel.checkPerm.observe(this,
                Observer { checkPermissionExtStorage() })

        viewModel.requestPermCmd.observe(this,
                Observer {  requestPermission() })

    }

    /*
     *********************************************************************************************
     * INNER
     *********************************************************************************************/
    @OnClick(R.id.cropped_img_preview)
    fun shotPreviewClick() {
        mShotEditionViewModel!!.onImagePreviewClicked()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST) {
                mShotEditionViewModel.imagePickedUriSource = ImagePicker.getImageUriFromResult(this, resultCode, data)
                mShotEditionViewModel.imagePickedFileName = ImagePicker.getPickedImageName(this, mShotEditionViewModel!!.imagePickedUriSource!!)
                mShotEditionViewModel.imagePickedFormat = ImageUtils.getImageExtension(this, mShotEditionViewModel!!.imagePickedUriSource!!)
                mShotEditionViewModel.imageSize = ImageUtils.imagePickedWidthHeight(this, mShotEditionViewModel!!.imagePickedUriSource!!, 0)

                mShotEditionViewModel.onImagePicked()
            } else if (requestCode == UCrop.REQUEST_CROP)
                mShotEditionViewModel.onImageCropped(UCrop.getOutput(data!!)!!)
        } else {
            if (resultCode == UCrop.RESULT_ERROR)
                mShotEditionViewModel.onPickcropError(0)
            else
                mShotEditionViewModel.onPickcropError(1)
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

    fun checkPermissionExtStorage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //todo single live event
            mShotEditionViewModel.requestPerm()
        } else {
            mShotEditionViewModel.onPermGranted()
        }
    }

    fun requestPermission() {
        requestPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_storage_rationale),
                Constants.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION)
    }

    fun notifyPostSaved() {
        Toast.makeText(this, "ShotDraft saved in memory: ", Toast.LENGTH_SHORT).show()
        finishAfterTransition()
    }

    fun openImagePicker() {
        ImagePicker.pickImage(this, Constants.PICK_IMAGE_REQUEST)
    }

    fun goToUCropActivity(imagePickedformat: String?,
                          imagePickedUriSource: Uri?,
                          imagePickedFileName: String?,
                          imageSize: IntArray?) {
        ImageUtils.goToUCropActivity(imagePickedformat,
                imagePickedUriSource!!,
                Uri.fromFile(File(cacheDir, imagePickedFileName!!)), this,
                imageSize!!)
    }

    fun showImageNotUpdatable() {
        Toast.makeText(this@EditShotActivity, "Shot can't be changed in edition mode", Toast.LENGTH_SHORT).show()
    }

    fun openConfirmMenu() {}

    fun stopActivity() {
        finish()
    }

    private fun setUpEditionUI(draft: Draft) {
        if (draft.typeOfDraft == Constants.EDIT_MODE_NEW_SHOT) {
            mToolbar.title = "Create a shot"
        } else {
            mToolbar.title = "Edit a shot"
        }
        if (draft.imageUri == null)
            croppedImagePreview.setImageResource(R.drawable.add_image_illustration)
        val imageCroppedUri :Uri? = EditUtils.getImageUrl(this, draft)
        if (imageCroppedUri!=null)
            mShotEditionViewModel.onImageCropped(imageCroppedUri)
        mShotTitleEditor.setText(draft.shot.title)
        //todo - from html only if source is from shot... --> mange it in task!!!
        val description = EditUtils.getDescription(draft)
        if (description != null && !description.isEmpty())
            mShotDescriptionEditor.setText(MyTextUtils.noTrailingwhiteLines(description))
        mTagEditor.setText(EditUtils.getTagList(draft))

    }

    private fun displayShotImagePreview(uriImageCropped: Uri?) {
        if (uriImageCropped != null) {
            Glide.with(mApplication)
                    .asBitmap()
                    .load(uriImageCropped)
                    .apply(RequestOptions()
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

    fun showMessageEmptyTitle() {
        Toast.makeText(this, "please define a title", Toast.LENGTH_SHORT).show()
    }

}
