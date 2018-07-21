package seigneur.gauvain.mycourt.di.modules;

import android.app.Application;
import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import seigneur.gauvain.mycourt.data.local.MyCourtDatabase;
import seigneur.gauvain.mycourt.data.local.dao.PostDao;
import seigneur.gauvain.mycourt.data.local.dao.TokenDao;
import seigneur.gauvain.mycourt.data.local.dao.UserDao;

@Module
public class RoomModule {

    @Provides
    @Singleton
    MyCourtDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application,
                MyCourtDatabase.class, "MyCourtDatabase.db")
                //.fallbackToDestructiveMigration()//cleared before migrate
                .build();
    }

    @Provides
    @Singleton
    PostDao providePostDao(MyCourtDatabase database) { return database.postDao(); }

    @Provides
    @Singleton
    UserDao provideUserDao(MyCourtDatabase database) { return database.userDao(); }

    @Provides
    @Singleton
    TokenDao provideTokenDao(MyCourtDatabase database) { return database.tokenDao(); }
}
