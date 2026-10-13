package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.LumoOnlineCatalogService
import com.example.data.LumoSettingsEntity
import com.example.data.LumoYouTubeStreamAuth
import com.example.data.MediaItemEntity
import com.example.ui.theme.ColorReactiveIntensity
import com.example.ui.theme.LumoColorReactiveState
import com.example.ui.theme.LumoThemeOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appContext_andThemes_areValid() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context)
        assertEquals(5, LumoThemeOption.entries.size)
    }

    @Test
    fun youtubeVideoIdExtraction_andExactStreamUrl_matchVideoId() {
        val service = LumoOnlineCatalogService()
        assertEquals("dQw4w9WgXcQ", service.extractYouTubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("4NRXx6U8ABQ", service.extractYouTubeVideoId("https://youtu.be/4NRXx6U8ABQ?si=12345"))
        assertEquals("FGBhQbmPwH8", service.extractYouTubeVideoId("https://www.youtube.com/shorts/FGBhQbmPwH8"))
        assertTrue(service.isLikelyYouTubeOrMediaUrl("https://youtu.be/4NRXx6U8ABQ"))

        val exactAudioUrl = LumoYouTubeStreamAuth.buildExactYouTubeStreamUrl("rvje5oblrLw", itag = 140)
        assertTrue(exactAudioUrl.contains("id=rvje5oblrLw"))
        assertTrue(exactAudioUrl.contains("itag=140"))

        val exactVideoUrl = LumoYouTubeStreamAuth.buildExactYouTubeVideoStreamUrl("rvje5oblrLw", itag = 134)
        assertTrue(exactVideoUrl.contains("id=rvje5oblrLw"))
        assertTrue(exactVideoUrl.contains("itag=134"))

        // Verify MediaItemEntity uses resolved CDN streamUrl and rejects offline invidious.f5.si proxy URLs
        val resolvedCdnUrl = "https://cf-media.sndcdn.com/live_vannda_time_to_rise.128.mp3"
        val ytItem = MediaItemEntity(
            id = "yt_rvje5oblrLw",
            title = "Time To Rise",
            artist = "VannDa",
            album = "YouTube",
            durationSec = 245,
            isVideo = false,
            streamUrl = resolvedCdnUrl
        )
        assertEquals(resolvedCdnUrl, ytItem.effectivePlayableUrlOrPath)
        assertEquals(resolvedCdnUrl, ytItem.effectiveVideoStreamUrlOrPath)

        val brokenProxyItem = ytItem.copy(streamUrl = "https://jp1-cmp.invidious.f5.si/latest_version?id=rvje5oblrLw&itag=140")
        assertEquals("", brokenProxyItem.effectivePlayableUrlOrPath)
    }

    @Test
    fun colorReactiveEngine_intensityModes_andScalingWorkCorrectly() {
        assertEquals(3, ColorReactiveIntensity.entries.size)
        val defaultSettings = LumoSettingsEntity()
        assertEquals(ColorReactiveIntensity.MEDIUM, defaultSettings.reactiveIntensity)

        assertEquals(ColorReactiveIntensity.HIGH, ColorReactiveIntensity.MEDIUM.next())
        assertEquals(ColorReactiveIntensity.LOW, ColorReactiveIntensity.HIGH.next())
        assertEquals(ColorReactiveIntensity.MEDIUM, ColorReactiveIntensity.LOW.next())

        val lowState = LumoColorReactiveState(
            bassLevel = 0.25f,
            beatPulse = 0.30f,
            intensityMode = ColorReactiveIntensity.LOW,
            isPlaying = true
        )
        val highState = LumoColorReactiveState(
            bassLevel = 0.80f,
            beatPulse = 0.85f,
            intensityMode = ColorReactiveIntensity.HIGH,
            isPlaying = true
        )
        assertTrue(highState.ambientGlowAlpha > lowState.ambientGlowAlpha)
        assertTrue(highState.beatScaleExpansion > lowState.beatScaleExpansion)
        assertTrue(highState.playButtonHaloScale > lowState.playButtonHaloScale)
    }

    @Test
    fun mainNavigation_andYouTubeDownloaderMode_canSwitch() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_screen_list").assertExists()

        // Cycle Color Reactive Engine intensity badge on Home header
        composeTestRule.onNodeWithTag("home_reactive_intensity_badge").assertExists().performClick()
        composeTestRule.waitForIdle()

        // Navigate to Search & Download tab
        composeTestRule.onNodeWithTag(com.example.ui.theme.LumoDestination.SEARCH.testTag).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_screen_list").assertExists()

        // Switch to YouTube Link Downloader mode
        composeTestRule.onNodeWithTag("mode_youtube_link_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("youtube_link_downloader_card").assertExists()
    }

    @Test
    fun worldTrending_usesOnlyGlobalPlays_andIgnoresSingleUserPlayCount() {
        val localOneUserTrack = MediaItemEntity(
            id = "local_user_only",
            title = "Local 1 User Song",
            artist = "Test Artist",
            album = "Single",
            durationSec = 200,
            isVideo = false,
            streamUrl = "https://cf-media.sndcdn.com/local.mp3",
            playCount = 999,
            popularityScore = 0L
        )
        val globalHitTrack1 = MediaItemEntity(
            id = "global_hit_1",
            title = "Global Song 1",
            artist = "Global Artist 1",
            album = "World Chart",
            durationSec = 210,
            isVideo = false,
            streamUrl = "https://cf-media.sndcdn.com/global1.mp3",
            playCount = 0,
            popularityScore = 85_000_000L
        )
        val globalHitTrack2 = MediaItemEntity(
            id = "global_hit_2",
            title = "Global Song 2",
            artist = "Global Artist 2",
            album = "World Chart",
            durationSec = 195,
            isVideo = false,
            streamUrl = "https://cf-media.sndcdn.com/global2.mp3",
            playCount = 0,
            popularityScore = 1_250_000_000L
        )

        // Single user's local playCount must never inflate global popularity
        assertEquals("Global Chart", localOneUserTrack.formattedPopularity)
        assertEquals("85.0M global plays", globalHitTrack1.formattedPopularity)
        assertEquals("1.25B global plays", globalHitTrack2.formattedPopularity)

        // Trending ranking must exclude 0-global-play tracks and order strictly by worldwide popularityScore
        val sortedTrending = com.example.data.KhmerArtistDirectory.sortTrendingNewFirst(
            listOf(localOneUserTrack, globalHitTrack1, globalHitTrack2)
        )
        assertEquals(2, sortedTrending.size)
        assertEquals("global_hit_2", sortedTrending[0].id)
        assertEquals("global_hit_1", sortedTrending[1].id)
    }

    @Test
    fun trendingFeedFilter_onlyHasTrendingTodayAnd2026_andExcludesOldYears() {
        val entries = com.example.data.TrendingFeedFilter.entries
        assertEquals(2, entries.size)
        assertTrue(entries.contains(com.example.data.TrendingFeedFilter.TRENDING_TODAY))
        assertTrue(entries.contains(com.example.data.TrendingFeedFilter.TRENDING_2026))

        val old2023Song = MediaItemEntity(
            id = "old_2023",
            title = "Old Hit (2023)",
            artist = "Global Artist",
            album = "Hits 2023",
            durationSec = 200,
            isVideo = false,
            streamUrl = "https://cf-media.sndcdn.com/old.mp3",
            releaseYear = 2023,
            popularityScore = 9_500_000L
        )
        val today2026Song = MediaItemEntity(
            id = "today_2026",
            title = "Fresh Hit 2026",
            artist = "VannDa",
            album = "Trending Today · 1.2M listeners",
            durationSec = 205,
            isVideo = false,
            streamUrl = "https://cf-media.sndcdn.com/2026.mp3",
            licenseBadge = "Trending Today · 1.2M listeners",
            category = "Trending Today",
            releaseYear = 2026,
            popularityScore = 1_200_000L
        )

        val sorted2026 = com.example.data.KhmerArtistDirectory.sortTrendingNewFirst(
            items = listOf(old2023Song, today2026Song),
            filter = com.example.data.TrendingFeedFilter.TRENDING_2026
        )
        assertEquals(1, sorted2026.size)
        assertEquals("today_2026", sorted2026.first().id)
    }
}
