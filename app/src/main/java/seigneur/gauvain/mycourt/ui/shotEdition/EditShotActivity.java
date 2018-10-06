package seigneur.gauvain.mycourt.ui.shotEdition;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.repository.ShotDraftRepository;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.data.repository.TempDataRepository;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.utils.ConnectivityReceiver;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.ImagePicker;
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView;
import seigneur.gauvain.mycourt.utils.ImageUtils;
import seigneur.gauvain.mycourt.utils.MyTextUtils;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

public class EditShotActivity extends BaseActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ShotEditionViewModel mShotEditionViewModel;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.main)
    CoordinatorLayout layoutMain;

    @BindView(R.id.shot_title_edt)
    TextInputEditText mShotTitleEditor;

    @BindView(R.id.shot_tag_edt)
    TextInputEditText mTagEditor;

    @BindView(R.id.shot_description_edt)
    TextInputEditText mShotDescriptionEditor;

    @BindView(R.id.cropped_img_preview)
    FourThreeImageView croppedImagePreview;

    @BindView(R.id.btn_store)
    Button storeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shot);
        ButterKnife.bind(this);
        //provide Dependencies
        AndroidInjection.inject(this);

        //Provide ViewModel
        mShotEditionViewModel = ViewModelProviders.of(this, viewModelFactory).get(ShotEditionViewModel.class);
        mShotEditionViewModel.init();
        //Subscribe to ViewModel data and event
        subscribeToLiveData(mShotEditionViewModel);
        subscribeToSingleEvent(mShotEditionViewModel);

        //Listen EditText
        mShotTitleEditor.addTextChangedListener(titleWatcher);
        mTagEditor.addTextChangedListener(tagWtacher);
        mShotDescriptionEditor.addTextChangedListener(descriptionWatcher);
    }

    @OnClick(R.id.btn_store)
    public void store() {
        mShotEditionViewModel.onStoreDraftClicked();
    }

    /*
    *********************************************************************************************
    * EVENT WHICH VIEW WILL SUBSCRIBE
    *********************************************************************************************/
    private void subscribeToLiveData(ShotEditionViewModel viewModel) {
        viewModel.getCroppedImageUri().observe(this, this::displayShotImagePreview);

        viewModel.getTitle().observe(this, title -> {
            Timber.d("title change:" +title);
        });

        viewModel.getDescription().observe(this,  desc -> {
            Timber.d("desc change:" +desc);
        });

        viewModel.getTags().observe(this, tags -> {
            Timber.d("tags change:" +tags);
        });
    }

    private void subscribeToSingleEvent(ShotEditionViewModel viewModel) {

        viewModel.getSetUpUiCmd().observe(this,
                call -> setUpShotEditionUI(
                        mShotEditionViewModel.getObjectSource())
        );

        viewModel.getPickShotCommand().observe(this, call -> openImagePicker());

        viewModel.getCropImageCmd().observe(this,
                call -> goToUCropActivity(
                        mShotEditionViewModel.imagePickedFormat,
                        mShotEditionViewModel.imagePickedUriSource,
                        mShotEditionViewModel.imagePickedFileName,
                        mShotEditionViewModel.imageSize));

        viewModel.getPickCropImgErrorCmd().observe(this,
                errorCode -> Toast.makeText(this, "oops :"+errorCode , Toast.LENGTH_SHORT).show());

        viewModel.getCheckPerm().observe(this,
                call -> checkPermissionExtStorage());

        viewModel.getRequestPermCmd().observe(this,
                call -> requestPermission());

    }

    /*
    *********************************************************************************************
    * INNER
    *********************************************************************************************/
    @OnClick(R.id.cropped_img_preview)
    public void shotPreviewClick() {
        mShotEditionViewModel.onImagePreviewClicked();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode== Constants.PICK_IMAGE_REQUEST) {
                mShotEditionViewModel.setImagePickedUriSource(ImagePicker.getImageUriFromResult(this, resultCode, data));
                mShotEditionViewModel.setImagePickedFileName(ImagePicker.getPickedImageName(this, mShotEditionViewModel.getImagePickedUriSource()));
                mShotEditionViewModel.setImagePickedFormat(ImageUtils.getImageExtension(this, mShotEditionViewModel.getImagePickedUriSource()));
                mShotEditionViewModel.setImageSize(ImageUtils.imagePickedWidthHeight(this, mShotEditionViewModel.getImagePickedUriSource(), 0));

                mShotEditionViewModel.onImagePicked();
            }
            else
                if (requestCode == UCrop.REQUEST_CROP)
                    mShotEditionViewModel.onImageCropped(UCrop.getOutput(data));
        } else {
            if (resultCode == UCrop.RESULT_ERROR)
                mShotEditionViewModel.onPickcropError(0);
            else
                mShotEditionViewModel.onPickcropError(1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Callback received when a permissions request has been completed.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mShotEditionViewModel.onPermGranted();
                } else {
                    mShotEditionViewModel.requestPerm();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void displayShotImagePreview(Uri uriImageCropped) {
        if (uriImageCropped!=null) {
            Glide
                    .with(this)
                    .asDrawable()
                    .load(uriImageCropped)
                    .apply(new RequestOptions()
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .placeholder(R.mipmap.ic_launcher)
                            //.error(R.mipmap.ic_launcher)
                    )
                    .into(croppedImagePreview);
        } else {
            croppedImagePreview.setImageResource(R.drawable.add_image_illustration);
        }
    }


    public void checkPermissionExtStorage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //todo single live event
            mShotEditionViewModel.requestPerm();
        } else {
            mShotEditionViewModel.onPermGranted();
        }
    }

    public void requestPermission() {
        requestPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_storage_rationale),
                Constants.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
    }

    public void notifyPostSaved() {
        Toast.makeText(this, "ShotDraft saved in memory: ", Toast.LENGTH_SHORT).show();
        finishAfterTransition();
    }

    public void setUpShotEditionUI(Object object) {
        //Listen text change
        mShotTitleEditor.addTextChangedListener(titleWatcher);
        mTagEditor.addTextChangedListener(tagWtacher);
        mShotDescriptionEditor.addTextChangedListener(descriptionWatcher);

        if (object!=null)  {
            mToolbar.setTitle("Edit a shot");
            setUpEditionUI(true, object);
        } else {
            mToolbar.setTitle("Create a shot");
            setUpCreationModeUI();
        }
    }

    public void openImagePicker() {
        ImagePicker.pickImage(this, Constants.PICK_IMAGE_REQUEST);
    }

    public void goToUCropActivity(String imagePickedformat,
                                  Uri imagePickedUriSource,
                                  String imagePickedFileName,
                                  int[] imageSize) {
        ImageUtils.goToUCropActivity(imagePickedformat,
                imagePickedUriSource,
                Uri.fromFile(new File(getCacheDir(), imagePickedFileName)),this,
                imageSize);
    }

    public void showImageNotUpdatable() {
        Toast.makeText(EditShotActivity.this, "Shot can't be changed in edition mode", Toast.LENGTH_SHORT).show();
    }

    public void openConfirmMenu() {
    }

    public void stopActivity() {
        finish();
    }

    /*
    *********************************************************************************************
    * UI - MANAGE EDITION MODE
    *********************************************************************************************/
    private void setUpCreationModeUI() {
        croppedImagePreview.setImageResource(R.drawable.add_image_illustration);
    }

    private void setUpEditionUI(final boolean isTransactionPostponed,Object object) {
        if (isTransactionPostponed) postponeEnterTransition();
        Glide.with(this)
                .asBitmap()
                .load(getImageUrl(object))
                .apply(new RequestOptions()
                        .error(R.drawable.ic_my_shot_black_24dp)
                )
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (isTransactionPostponed) startPostponedEnterTransition();
                        Toast.makeText(EditShotActivity.this, "error loading image", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (isTransactionPostponed) startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(croppedImagePreview);

        mTagEditor.setText(getTagList(object));
        mShotTitleEditor.setText(getTitle(object));
        String description = getDescription(object);
        if (description!=null)
            mShotDescriptionEditor.setText(MyTextUtils.noTrailingwhiteLines(description));
    }


    //get title from data sent by presenter
    //todo - mange this in Viewmodel to alwas send the same livedata
    public String getTitle(Object object){
        if (object instanceof Shot) {
            Shot shot = (Shot) object;
            return shot.getTitle();
        } else if(object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            return shotDraft.getTitle();
        } else {
            return null;
        }
    }

    //get image uri from data sent by presenter
    public Uri getImageUrl(Object object) {
        if (object instanceof Shot){
            Shot shot = (Shot) object;
            return Uri.parse(shot.getImageUrl());
        }
        else if (object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            if (shotDraft.getImageUrl()!=null) {
                if (shotDraft.getDraftType()==Constants.EDIT_MODE_NEW_SHOT) {
                    return FileProvider.getUriForFile(
                            this,
                            this.getString(R.string.file_provider_authorities),
                            new File(shotDraft.getImageUrl()));
                }
                else {
                    Toast.makeText(this, "is sshotdraft not new"+shotDraft.getDraftType(), Toast.LENGTH_SHORT).show();
                    return Uri.parse(shotDraft.getImageUrl());
                }

            }
            else
                return null;
        }
        else
            return null;
    }

    //get tags from data sent by presenter
    public StringBuilder getTagList(Object object){
        StringBuilder stringBuilder = new StringBuilder();
        if (object instanceof Shot) {
            Shot shot = (Shot) object;
            stringBuilder = adaptTagListToEditText(shot.getTagList());
        } else if(object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            stringBuilder =adaptTagListToEditText(shotDraft.getTagList());
        }
        return stringBuilder;
    }

    /**
     * Check if a tag contains more than one word, if true, add double quote to it,
     * @param tagList - list from Shot or ShotDraft
     * @return string from list with each item separated by a comma
     */
    private StringBuilder adaptTagListToEditText (ArrayList<String> tagList) {
        StringBuilder listString = new StringBuilder();
        Pattern multipleWordTagPattern = Pattern.compile(MyTextUtils.multipleWordtagRegex);
        for (String s : tagList) {
            Matcher wordMatcher = multipleWordTagPattern.matcher(s);
            if (!wordMatcher.matches()) {
                s = "\""+ s +"\"";
            }
            listString.append(s+", ");
        }
        return listString;
    }

    public String getDescription(Object object){
        String desc = null;
        if (object instanceof Shot) {
            Shot shot = (Shot) object;
            desc= Html.fromHtml(shot.getDescription()).toString();
        } else if(object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            desc =  shotDraft.getDescription();
        }
        return desc;
    }

    public void showMessageEmptyTitle() {
        Toast.makeText(this, "please define a title", Toast.LENGTH_SHORT).show();
    }

    /*
     *********************************************************************************************
     * TEXTWATCHER
     *********************************************************************************************/
    private final TextWatcher titleWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mShotEditionViewModel.onTitleChanged(s.toString());
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher tagWtacher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //todo - must manage tag limit in ViewmODEL
            String tagString = s.toString();
            int commas = s.toString().replaceAll("[^,]","").length();
            Timber.d(commas+"");
            if (commas<12) {
                mShotEditionViewModel.onTagChanged(tagString);
            } else {

            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher descriptionWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mShotEditionViewModel.onDescriptionChanged(s.toString());
        }

        public void afterTextChanged(Editable s) {
        }
    };

}


