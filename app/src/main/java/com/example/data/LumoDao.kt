package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LumoDao {
    @Query("SELECT * FROM media_items ORDER BY isVideo ASC, popularityScore DESC, lastPlayedTimestamp DESC, title ASC")
    fun observeAllMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY isVideo ASC, popularityScore DESC, lastPlayedTimestamp DESC, title ASC")
    fun observeFavoriteMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC")
    fun observeHistoryMedia(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: String): MediaItemEntity?

    @Query("SELECT * FROM media_items")
    suspend fun getAllMediaOnce(): List<MediaItemEntity>

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun countMediaItems(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE popularityScore > 0")
    suspend fun countPopularMediaItems(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE category LIKE '%Khmer%' OR artist LIKE '%(%'")
    suspend fun countKhmerMediaItems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedia(items: List<MediaItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedia(item: MediaItemEntity)

    @Update
    suspend fun updateMedia(item: MediaItemEntity)

    @Query("DELETE FROM media_items WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: String)

    @Query("DELETE FROM media_items WHERE durationSec <= 0")
    suspend fun removeShortPreviewItems()

    @Query("DELETE FROM media_items WHERE streamUrl LIKE '%invidious.f5.si%'")
    suspend fun removeMismatchedYouTubeItems()

    @Query("DELETE FROM media_items WHERE (id LIKE 'jiosaavn_%' OR id LIKE 'audius_%') AND localFilePath = '' AND isFavorite = 0 AND inLibrary = 0")
    suspend fun removeLegacyCatalogItems()

    @Query("DELETE FROM media_items WHERE localFilePath = '' AND isFavorite = 0 AND inLibrary = 0 AND lastPlayedTimestamp = 0")
    suspend fun purgeUnsavedCatalogItems()

    @Query("DELETE FROM downloads WHERE status = 'PAUSED' AND remoteUrl LIKE '%invidious.f5.si%'")
    suspend fun removeBrokenProxyDownloads()

    @Query("DELETE FROM media_items WHERE localFilePath = '' AND isFavorite = 0 AND inLibrary = 0")
    suspend fun deleteUnsavedSearchAndCatalogCache(): Int

    @Query("UPDATE media_items SET lastPlayedTimestamp = 0, lastPositionSec = 0 WHERE lastPlayedTimestamp > 0")
    suspend fun clearListeningHistory()

    @Query("DELETE FROM downloads WHERE status != 'COMPLETED'")
    suspend fun deleteIncompleteDownloads()

    @Query("UPDATE media_items SET streamUrl = :streamUrl WHERE id = :mediaId")
    suspend fun updateStreamUrl(mediaId: String, streamUrl: String)

    @Query("UPDATE media_items SET isFavorite = :isFavorite, inLibrary = CASE WHEN :isFavorite = 1 THEN 1 ELSE inLibrary END WHERE id = :mediaId")
    suspend fun setFavorite(mediaId: String, isFavorite: Boolean)

    @Query("UPDATE media_items SET localFilePath = :localPath, inLibrary = 1 WHERE id = :mediaId")
    suspend fun setLocalFilePath(mediaId: String, localPath: String)

    @Query("UPDATE media_items SET lastPlayedTimestamp = :timestamp, lastPositionSec = :positionSec, playCount = playCount + 1 WHERE id = :mediaId")
    suspend fun recordPlayback(mediaId: String, timestamp: Long, positionSec: Int)

    @Query("UPDATE media_items SET lastPositionSec = :positionSec WHERE id = :mediaId")
    suspend fun updatePlaybackPosition(mediaId: String, positionSec: Int)

    @Query("UPDATE media_items SET durationSec = :durationSec WHERE id = :mediaId")
    suspend fun updateDuration(mediaId: String, durationSec: Int)

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAllDownloads(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(task: DownloadTaskEntity)

    @Query("UPDATE downloads SET title = :newTitle WHERE id = :id")
    suspend fun renameDownload(id: String, newTitle: String)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'")
    suspend fun clearCompletedDownloads()

    // Settings
    @Query("SELECT * FROM lumo_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<LumoSettingsEntity?>

    @Query("SELECT * FROM lumo_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): LumoSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: LumoSettingsEntity)
}
