package seigneur.gauvain.mycourt.di.modules

import android.app.Application
import androidx.room.Room

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import seigneur.gauvain.mycourt.data.local.MyCourtDatabase
import seigneur.gauvain.mycourt.data.local.dao.PinDao
import seigneur.gauvain.mycourt.data.local.dao.PostDao
import seigneur.gauvain.mycourt.data.local.dao.TokenDao
import seigneur.gauvain.mycourt.data.local.dao.UserDao

@Module
class RoomModule {

    @Provides
    @Singleton
    internal fun provideDatabase(application: Application): MyCourtDatabase {
        return Room.databaseBuilder(application,
                MyCourtDatabase::class.java, "MyCourtDatabase.db")
                //.fallbackToDestructiveMigration()//cleared before migrate
                .build()
    }

    @Provides
    @Singleton
    internal fun providePostDao(database: MyCourtDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    @Singleton
    internal fun provideUserDao(database: MyCourtDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    internal fun provideTokenDao(database: MyCourtDatabase): TokenDao {
        return database.tokenDao()
    }


    @Provides
    @Singleton
    internal fun providePinDao(database: MyCourtDatabase): PinDao {
        return database.pinDao()
    }
}
