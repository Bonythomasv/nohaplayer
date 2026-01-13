package com.noha.player.di

import android.content.Context
import com.noha.player.data.local.FavoritesStore
import com.noha.player.data.local.PlaylistStore
import com.noha.player.data.local.SettingsStore
import com.noha.player.data.repository.IPTVRepository
import com.noha.player.data.repository.IPTVService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://iptv-org.github.io/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideIPTVService(retrofit: Retrofit): IPTVService {
        return retrofit.create(IPTVService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideIPTVRepository(
        iptvService: IPTVService,
        @ApplicationContext context: Context
    ): IPTVRepository {
        return IPTVRepository(iptvService, context)
    }

    @Provides
    @Singleton
    fun provideFavoritesStore(
        @ApplicationContext context: Context
    ): FavoritesStore = FavoritesStore(context)

    @Provides
    @Singleton
    fun providePlaylistStore(
        @ApplicationContext context: Context
    ): PlaylistStore = PlaylistStore(context)

    @Provides
    @Singleton
    fun provideSettingsStore(
        @ApplicationContext context: Context
    ): SettingsStore = SettingsStore(context)
}

