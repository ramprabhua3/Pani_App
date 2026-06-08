package com.pani.app.di

import com.pani.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Single [SupabaseClient] instance for the app lifetime.
     *
     * Credentials flow:
     *   secrets.properties → Gradle BuildConfig injection → BuildConfig.SUPABASE_*
     *
     * Installed plugins:
     *   - Postgrest  : table reads/writes + RPC (ST_DWithin radius queries)
     *   - Auth       : Firebase phone OTP tokens are exchanged here after verification
     *   - Storage    : Hunar video + thumbnail uploads
     *   - Realtime   : chat subscriptions (Phase 3)
     */
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
        install(Realtime)
    }
}
