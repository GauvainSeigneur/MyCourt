package seigneur.gauvain.mycourt.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import seigneur.gauvain.mycourt.data.local.dao.PinDao;
import seigneur.gauvain.mycourt.data.local.dao.PostDao;
import seigneur.gauvain.mycourt.data.local.dao.TokenDao;
import seigneur.gauvain.mycourt.data.local.dao.UserDao;
import seigneur.gauvain.mycourt.data.model.Pin;
import seigneur.gauvain.mycourt.data.model.ShotDraft;
import seigneur.gauvain.mycourt.data.model.Token;
import seigneur.gauvain.mycourt.data.model.User;
import seigneur.gauvain.mycourt.utils.RoomConverter;

@Database(entities = {ShotDraft.class, User.class, Token.class, Pin.class}, version = 1)
@TypeConverters(RoomConverter.class)
public abstract class MyCourtDatabase extends RoomDatabase {

    // SINGLETON
    private static volatile MyCourtDatabase INSTANCE;

    // DAO
    public abstract PostDao postDao();

    // DAO
    public abstract UserDao userDao();

    // DAO
    public abstract TokenDao tokenDao();

    // DAO
    public abstract PinDao pinDao();
}

