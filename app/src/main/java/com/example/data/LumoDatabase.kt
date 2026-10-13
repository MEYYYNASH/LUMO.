package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MediaItemEntity::class,
        PlaylistEntity::class,
        DownloadTaskEntity::class,
        LumoSettingsEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class LumoDatabase : RoomDatabase() {
    abstract fun lumoDao(): LumoDao

    companion object {
        @Volatile
        private var INSTANCE: LumoDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                runCatching {
                    db.execSQL("ALTER TABLE lumo_settings ADD COLUMN appLanguage TEXT NOT NULL DEFAULT 'KM'")
                }
            }
        }

        fun getInstance(context: Context): LumoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LumoDatabase::class.java,
                    "lumo_media_database.db"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
