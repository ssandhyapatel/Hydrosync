package com.hydrosync.mobile.di

import android.content.Context
import androidx.room.Room
import com.hydrosync.mobile.ble.BleManager
import com.hydrosync.mobile.data.AlertDao
import com.hydrosync.mobile.data.AppDatabase
import com.hydrosync.mobile.data.SensorDao
import com.hydrosync.mobile.wellness.WellnessStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase {
        return Room.databaseBuilder(ctx, AppDatabase::class.java, "hydrosync-db")
            .fallbackToDestructiveMigration() // dev convenience; replace with migrations in prod
            .build()
    }
    @Provides
    fun providePredictionDao(database: AppDatabase): PredictionDao {
        return database.predictionDao()
    }

    @Provides
    @Singleton
    fun providePredictionRepository(dao: PredictionDao): com.hydrosync.mobile.repo.PredictionRepository {
        return com.hydrosync.mobile.repo.PredictionRepository(dao)
    }

    @Provides
    fun provideSensorDao(db: AppDatabase): SensorDao = db.sensorDao()

    @Provides
    fun provideAlertDao(db: AppDatabase): AlertDao = db.alertDao()

    @Provides
    @Singleton
    fun provideBleManager(@ApplicationContext ctx: Context): BleManager = BleManager(ctx)

    @Provides
    @Singleton
    fun provideWellnessStore(@ApplicationContext ctx: Context): WellnessStore = WellnessStore(ctx)

    @Provides
    @Singleton
    fun provideBlePreferences(@ApplicationContext context: Context): com.hydrosync.mobile.ble.BlePreferences {
        return com.hydrosync.mobile.ble.BlePreferences(context)
    }
}
