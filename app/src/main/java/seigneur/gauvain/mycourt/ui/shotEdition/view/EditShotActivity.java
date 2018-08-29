package seigneur.gauvain.mycourt.ui.shotEdition.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import seigneur.gauvain.mycourt.R;
import seigneur.gauvain.mycourt.data.api.DribbbleService;
import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.local.dao.PostDao;
import seigneur.gauvain.mycourt.data.repository.ShotRepository;
import seigneur.gauvain.mycourt.ui.base.BaseActivity;
import seigneur.gauvain.mycourt.utils.Constants;
import seigneur.gauvain.mycourt.utils.ImagePicker;
import seigneur.gauvain.mycourt.ui.shotEdition.presenter.EditShotPresenter;
import seigneur.gauvain.mycourt.ui.widget.FourThreeImageView;
import seigneur.gauvain.mycourt.utils.ImageUtils;
import seigneur.gauvain.mycourt.utils.MyTextUtils;
import seigneur.gauvain.mycourt.utils.rx.NetworkErrorHandler;
import timber.log.Timber;

public class EditShotActivity extends BaseActivity implements EditShotView {

    @Inject
    EditShotPresenter mEditShotPresenter;

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
    FourThreeImageView croppedImagepreview;

    @BindView(R.id.confirm_action_layout)
    FrameLayout confirmActionLayout;

    @BindView(R.id.fab_draft)
    FloatingActionButton fabDraft;

    @BindView(R.id.fab_publish)
    FloatingActionButton fabPublish;

    @BindView(R.id.layout_buttons)
    FrameLayout layoutButtons;

    @BindView(R.id.scrim_view)
    View scrimView;

    @BindView(R.id.fab_confirm)
    FloatingActionButton fabConfirm;

    //for confirmation menu
    private boolean isConfirmMenuOpen = false;
    private int revealX;
    private int revealY;
    private int openStartRadius;
    private int openEndRadius;
    private int closeStartRadius;
    private int closeEndRadius;
    private Animator openAnim, closeAnim;
    private ObjectAnimator openFadeIn, closeFadeOut;
    private AnimatorSet openAnimatorSet, closeAnimatorSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shot);
        ButterKnife.bind(this);
        //editor must be set before presenter first method to check text change!
        mShotTitleEditor.addTextChangedListener(titleWatcher);
        mTagEditor.addTextChangedListener(tagWtacher);
        mShotDescriptionEditor.addTextChangedListener(descriptionWatcher);
        mEditShotPresenter.onAttach();
    }

    @OnClick(R.id.fab_confirm)
    public void confirmEdition() {
        mEditShotPresenter.onConfirmEditionClicked(true);
    }

    @OnClick(R.id.fab_draft)
    public void draftShot() {
        mEditShotPresenter.onDraftShotClicked(this);
    }

    @OnClick(R.id.fab_publish)
    public void publishShot() {
        mEditShotPresenter.onPublishClicked(this);
    }

    @OnClick(R.id.cropped_img_preview)
    public void shotPreviewClick() {
        mEditShotPresenter.onIllustrationClicked();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode== Constants.PICK_IMAGE_REQUEST)
                mEditShotPresenter.onImagePicked(this, requestCode, resultCode, data);
            else
                mEditShotPresenter.onImageCropped(requestCode,resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEditShotPresenter.onDetach();
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
                    //saveCroppedImage(resultCropUri);
                    mEditShotPresenter.onPermissionGranted(this);
                } else {
                    mEditShotPresenter.onPermissionDenied();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
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
                    .into(croppedImagepreview);
        } else {
            croppedImagepreview.setImageResource(R.drawable.add_image_illustration);
        }
    }

    @Override
    public void checkPermissionExtStorage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            mEditShotPresenter.onPermissionDenied();
        } else {
            mEditShotPresenter.onPermissionGranted(this);
        }
    }

    @Override
    public void requestPermission() {
        requestPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_storage_rationale),
                Constants.REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
    }

    @Override
    public void notifyPostSaved() {
        Toast.makeText(this, "ShotDraft saved in memory: ", Toast.LENGTH_SHORT).show();
        finishAfterTransition();
    }

    @Override
    public void setUpShotEditionUI(Object object, int source) {
        mToolbar.setTitle("Edit a shot");
        setUpEditionUI(true, object, source);
    }

    @Override
    public void setUpShotCreationUI() {
        mToolbar.setTitle("Create a shot");
        setUpCreationModeUI();
    }

    @Override
    public void openImagePicker() {
        ImagePicker.pickImage(this, Constants.PICK_IMAGE_REQUEST);
    }

    @Override
    public void goToUCropActivity(String imagePickedformat,
                                  Uri imagePickedUriSource,
                                  String imagePickedFileName,
                                  int[] imageSize) {
        ImageUtils.goToUCropActivity(imagePickedformat,
                imagePickedUriSource,
                Uri.fromFile(new File(getCacheDir(), imagePickedFileName)),this,
                imageSize);
    }

    @Override
    public void showImageNotUpdatable() {
        Toast.makeText(EditShotActivity.this, "Shot can't be changed in edition mode", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void openConfirmMenu() {
        showHideConfirmMenu();
    }

    @Override
    public void stopActivity() {
        finish();
    }

    /********************************************************************
     * ACTIVITY PRIVATE METHODS
     *******************************************************************/
    private void showHideConfirmMenu() {
        final int[] stateSet = {android.R.attr.state_checked * (!isConfirmMenuOpen ? 1 : -1)};
        fabConfirm.setImageState(stateSet, true);
        revealX = fabConfirm.getLeft()+fabConfirm.getWidth()/2;
        revealY = fabConfirm.getTop()+fabConfirm.getHeight()/2;
        openStartRadius = 0;
        openEndRadius = (int) Math.hypot(layoutMain.getWidth(), layoutMain.getHeight());
        closeStartRadius = Math.max(layoutMain.getWidth(), layoutMain.getHeight());
        closeEndRadius=0;
        if (!isConfirmMenuOpen) {
            //fab.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),android.R.color.white,null)));
            //fab.setImageResource(R.drawable.ic_close_grey);
            openAnim = ViewAnimationUtils.createCircularReveal(layoutButtons, revealX, revealY, openStartRadius, openEndRadius);
            openFadeIn = ObjectAnimator.ofFloat(scrimView, "alpha", 0f, 1f);
            openAnimatorSet = new AnimatorSet();
            openAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            openAnimatorSet.play(openAnim).with(openFadeIn);
            openAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    confirmActionLayout.setVisibility(View.VISIBLE);
                    //scrimView.setVisibility(View.VISIBLE);
                    fabConfirm.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isConfirmMenuOpen = true;
                    fabConfirm.setClickable(true);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    fabConfirm.setClickable(true);
                    isConfirmMenuOpen = false;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            openAnimatorSet.start();

        } else {
            //fab.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),R.color.colorAccent,null)));
            //fab.setImageResource(R.drawable.ic_plus_white);
            closeAnim = ViewAnimationUtils.createCircularReveal(layoutButtons, revealX, revealY, closeStartRadius, closeEndRadius);
            closeFadeOut = ObjectAnimator.ofFloat(scrimView, "alpha", 1f, 0f);
            closeAnimatorSet = new AnimatorSet();
            closeAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            closeAnimatorSet.play(closeAnim).with(closeFadeOut);
            closeAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    fabConfirm.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    confirmActionLayout.setVisibility(View.GONE);
                    // scrimView.setVisibility(View.GONE);
                    fabConfirm.setClickable(true);
                    isConfirmMenuOpen = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    fabConfirm.setClickable(true);
                    isConfirmMenuOpen = true;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            closeAnimatorSet.start();
        }
    }

    /********************************************************************
     * UI - EDITION MODE
     *******************************************************************/
    private void setUpEditionUI(final boolean isTransactionPostponed,Object object, int source) {

        if (isTransactionPostponed)
            postponeEnterTransition();
       // Toast.makeText(this, ""+url, Toast.LENGTH_SHORT).show();

        /**
         *  AS shot image is loaded from Glide ressource, put listener to define when to start startPostponedEnterTransition
         */
        Glide
                .with(this)
                .asBitmap()
                .load(getImageUrl(object, source))
                .apply(new RequestOptions()
                        .error(R.drawable.ic_my_shot_black_24dp)
                )
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (isTransactionPostponed) startPostponedEnterTransition();
                        //croppedImagepreview.setImageResource(R.drawable.ic_my_shot_black_24dp);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (isTransactionPostponed) startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(croppedImagepreview);

        mTagEditor.setText(getTagList(object));
        mShotTitleEditor.setText(getTitle(object));
        String description = getDescription(object);
        if (description!=null)
            mShotDescriptionEditor.setText(MyTextUtils.noTrailingwhiteLines(description));
    }


    //get title from data sent by presenter
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
    public Uri getImageUrl(Object object, int source) {
        if (object instanceof Shot){
            Shot shot = (Shot) object;
            return Uri.parse(shot.getImageUrl());
        }
        else if(object instanceof ShotDraft) {
            ShotDraft shotDraft = (ShotDraft) object;
            if (shotDraft.getImageUrl()!=null) {
                if (source==Constants.EDIT_MODE_NEW_SHOT)
                    return FileProvider.getUriForFile(this,
                            this.getString(R.string.file_provider_authorities),
                            new File(shotDraft.getImageUrl()));
                else
                    return Uri.parse(shotDraft.getImageUrl());
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
    /********************************************************************
     * UI - CREATION MODE
     *******************************************************************/
    private void setUpCreationModeUI() {
        croppedImagepreview.setImageResource(R.drawable.add_image_illustration);
    }

    /********************************************************************
     * TextWatcher
     *******************************************************************/
    private final TextWatcher titleWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mEditShotPresenter.onTitleChanged(s.toString());
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher tagWtacher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String tagString = s.toString();
            int commas = s.toString().replaceAll("[^,]","").length();
            Timber.d(commas+"");
            if (commas<12) {
                mEditShotPresenter.onTagChanged(tagString);
            } else {
                mEditShotPresenter.onTagLimitReached();
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher descriptionWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mEditShotPresenter.onDescriptionChanged(s.toString());
        }

        public void afterTextChanged(Editable s) {
        }
    };
}


