package seigneur.gauvain.mycourt.data.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import seigneur.gauvain.mycourt.data.model.Shot;
import seigneur.gauvain.mycourt.data.model.ShotDraft;

/**
 * Class used to get reference of data which are not
 * saved in DB or SharedPrefs
 * eg data send between presenters
 */
@Singleton
public class TempDataRepository {

    //Used to temporarily store Shot in order to send it between presenter...
    public Shot shot;
    //Used to temporarily store ShotDraft in order to send it between presenter...
    public ShotDraft mShotDraft;

    public int mDraftCallingSource;

    @Inject
    public TempDataRepository() {}

    public void setShot(Shot inShot){
        shot=inShot;
    }

    public Shot getShot() {return shot;}

    public void setShotDraft(ShotDraft shotDraft){
        mShotDraft=shotDraft;
    }

    public ShotDraft getShotDraft() {return mShotDraft;}

    public void setDraftCallingSource(int draftCallingSource) {
        mDraftCallingSource= draftCallingSource;
    }

    public int getDraftCallingSource() {
        return mDraftCallingSource;
    }

}
