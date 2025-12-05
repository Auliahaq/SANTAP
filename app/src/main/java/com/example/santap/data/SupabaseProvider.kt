package com.example.santap.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {

    // URL Proyek Supabase
    private const val SUPABASE_URL = "https://nwuranqbagutqmmuvedj.supabase.co"

    // Kunci API 'anon public' Supabase
    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im53dXJhbnFiYWd1dHFtbXV2ZWRqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ4NTAxNzgsImV4cCI6MjA4MDQyNjE3OH0.7DWYfg3fgZSI7cUOlLr_woik4Y0CwSp0s1gYd5EYRgc"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Storage)
        }
    }
}