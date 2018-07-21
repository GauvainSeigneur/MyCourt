package seigneur.gauvain.mycourt.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Constants {
    /**
     * API
     */
    public static final String HEADER_CACHE = "android-cache";
    public static final String RESPONSE_CACHE_DELAY = "ResponseCacheDelay";
    // Dribbble loads everything in a 12-per-page manner
    public static final int COUNT_PER_PAGE = 12;
    public static final String HEADER_SHOT_ID = "ShotID";

    /**
     * INTERNAL CONSTANTS
     */
    public static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    public static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    public static final int NO_TEAM_ID = -1;

    public static final int PICK_IMAGE_ID = 234; // the number doesn't matter

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EDIT_MODE_NEW_SHOT, EDIT_MODE_UPDATE_SHOT})
    public @interface EditionMode {}
    public static final int EDIT_MODE_NEW_SHOT = 501;
    public static final int EDIT_MODE_UPDATE_SHOT = 502;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EDIT_PUBLISHED, EDIT_DRAFTED, EDIT_ABORTED})
    public @interface EditResult {}
    public static final int EDIT_PUBLISHED = 601;
    public static final int EDIT_DRAFTED = 602;
    public static final int EDIT_ABORTED = 603;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SOURCE_DRAFT, SOURCE_SHOT, SOURCE_FAB})
    public @interface DraftCallingSource {}
    public static final int SOURCE_DRAFT            = 701; //from an already saved draft
    public static final int SOURCE_SHOT             = 702; //from a shot
    public static final int SOURCE_FAB              = 703; //from a fab (new draft)

    public static final String INSTAGRAM = "instagram";
    public static final String FACEBOOK = "facebook";
    public static final String GITHUB = "github";
    public static final String TWITTER = "twitter";
    public static final String CREATIVEMARKET = "creative market";
    public static final String MEDIUM = "medium";
    public static final String BEHANCE = "behance";
    public static final String LINKEDIN = "linkedin";


    //pagination tests
    public static final int PAGE_START = 1;
    public static int CURRENT_PAGE = PAGE_START;

    //use it like this in activity
    /*@EditionMode
    public abstract int getEditionMode();
    // Attach the annotation
    public abstract void setEditionMode(@EditionMode int mode);*/

}
