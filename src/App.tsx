import React, { useState, useEffect } from 'react';
import { MediaItem, Playlist, DownloadTask, LumoSettings, KhmerArtistProfile } from './types';
import { INITIAL_MEDIA_ITEMS, INITIAL_PLAYLISTS, INITIAL_SETTINGS, LUMO_THEMES } from './data/lumoData';
import { Navbar } from './components/Navbar';
import { MiniPlayer } from './components/MiniPlayer';
import { MusicPlayerModal } from './components/MusicPlayerModal';
import { LiquidGlassNav } from './components/LiquidGlassNav';
import { HomeScreen } from './screens/HomeScreen';
import { SearchScreen } from './screens/SearchScreen';
import { LibraryScreen } from './screens/LibraryScreen';
import { DownloadsScreen } from './screens/DownloadsScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { ArtistDetailScreen } from './screens/ArtistDetailScreen';

export function App() {
  const [mediaItems, setMediaItems] = useState<MediaItem[]>(INITIAL_MEDIA_ITEMS);
  const [playlists, setPlaylists] = useState<Playlist[]>(INITIAL_PLAYLISTS);
  const [downloads, setDownloads] = useState<DownloadTask[]>([]);
  const [settings, setSettings] = useState<LumoSettings>(INITIAL_SETTINGS);

  const [currentTab, setCurrentTab] = useState<string>('home');
  const [selectedArtist, setSelectedArtist] = useState<KhmerArtistProfile | null>(null);

  const [currentTrack, setCurrentTrack] = useState<MediaItem | null>(null);
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentSec, setCurrentSec] = useState<number>(0);
  const [isExpandedPlayerOpen, setIsExpandedPlayerOpen] = useState<boolean>(false);

  // Active theme details
  const activeTheme = LUMO_THEMES.find((th) => th.id === settings.themeId) || LUMO_THEMES[0];

  // Playback timer simulation
  useEffect(() => {
    let interval: any;
    if (isPlaying && currentTrack) {
      interval = setInterval(() => {
        setCurrentSec((prev) => {
          if (prev >= currentTrack.durationSec) {
            setIsPlaying(false);
            return 0;
          }
          return prev + 1;
        });
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [isPlaying, currentTrack]);

  const handlePlayTrack = (track: MediaItem) => {
    setCurrentTrack(track);
    setCurrentSec(0);
    setIsPlaying(true);
  };

  const handleTogglePlay = () => {
    setIsPlaying(!isPlaying);
  };

  const handleNext = () => {
    if (!currentTrack) return;
    const currentIndex = mediaItems.findIndex((m) => m.id === currentTrack.id);
    const nextIndex = (currentIndex + 1) % mediaItems.length;
    handlePlayTrack(mediaItems[nextIndex]);
  };

  const handlePrev = () => {
    if (!currentTrack) return;
    const currentIndex = mediaItems.findIndex((m) => m.id === currentTrack.id);
    const prevIndex = (currentIndex - 1 + mediaItems.length) % mediaItems.length;
    handlePlayTrack(mediaItems[prevIndex]);
  };

  const handleSeek = (seconds: number) => {
    setCurrentSec(seconds);
  };

  const handleToggleFavorite = (trackId: string) => {
    setMediaItems((prev) =>
      prev.map((item) => (item.id === trackId ? { ...item, isFavorite: !item.isFavorite } : item))
    );
  };

  const handleDownload = (track: MediaItem) => {
    const newTask: DownloadTask = {
      id: `dl_${Date.now()}`,
      mediaId: track.id,
      title: track.title,
      artist: track.artist,
      artworkUrl: track.artworkUrl,
      format: settings.defaultDownloadFormat,
      isVideo: track.isVideo,
      totalBytes: 15485760,
      downloadedBytes: 15485760,
      progressPercent: 100,
      status: 'COMPLETED',
      localFilePath: `/internal/storage/lumo/${track.id}.mp3`,
      createdAt: Date.now(),
    };
    setDownloads((prev) => [newTask, ...prev]);
    alert(`Successfully downloaded authorized media: ${track.title}`);
  };

  const handleCreatePlaylist = (name: string, description: string) => {
    const newPl: Playlist = {
      id: `pl_${Date.now()}`,
      name,
      description,
      coverUrl: mediaItems[0]?.artworkUrl || '',
      accentHex: '#007AFF',
      trackIds: [mediaItems[0]?.id || ''],
      isFavorite: false,
      createdAt: Date.now(),
    };
    setPlaylists((prev) => [newPl, ...prev]);
  };

  const handleUpdateSettings = (newSettings: Partial<LumoSettings>) => {
    setSettings((prev) => ({ ...prev, ...newSettings }));
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans pb-32">
      <Navbar
        currentTab={currentTab}
        onTabChange={(tab) => {
          setCurrentTab(tab);
          setSelectedArtist(null);
        }}
        activeThemeName={activeTheme.name}
        onOpenThemeModal={() => setCurrentTab('settings')}
      />

      <main className="flex-1 max-w-7xl mx-auto w-full px-6 py-8">
        {selectedArtist ? (
          <ArtistDetailScreen
            artist={selectedArtist}
            mediaItems={mediaItems}
            onBack={() => setSelectedArtist(null)}
            onPlayTrack={handlePlayTrack}
            onToggleFavorite={handleToggleFavorite}
          />
        ) : (
          <>
            {currentTab === 'home' && (
              <HomeScreen
                mediaItems={mediaItems}
                playlists={playlists}
                onPlayTrack={handlePlayTrack}
                onSelectArtist={(artist) => setSelectedArtist(artist)}
                onToggleFavorite={handleToggleFavorite}
                onOpenPlaylist={() => setCurrentTab('library')}
              />
            )}
            {currentTab === 'search' && (
              <SearchScreen
                mediaItems={mediaItems}
                onPlayTrack={handlePlayTrack}
                onToggleFavorite={handleToggleFavorite}
              />
            )}
            {currentTab === 'library' && (
              <LibraryScreen
                mediaItems={mediaItems}
                playlists={playlists}
                onPlayTrack={handlePlayTrack}
                onToggleFavorite={handleToggleFavorite}
                onCreatePlaylist={handleCreatePlaylist}
                onOpenPlaylist={() => {}}
              />
            )}
            {currentTab === 'downloads' && (
              <DownloadsScreen
                downloads={downloads}
                mediaItems={mediaItems}
                onPlayTrack={handlePlayTrack}
              />
            )}
            {currentTab === 'settings' && (
              <SettingsScreen
                settings={settings}
                onUpdateSettings={handleUpdateSettings}
              />
            )}
          </>
        )}
      </main>

      {/* Floating Liquid Glass Bottom Navigation Bar */}
      <LiquidGlassNav
        currentTab={currentTab}
        onTabChange={(tab) => {
          setCurrentTab(tab);
          setSelectedArtist(null);
        }}
      />

      {/* Mini Player */}
      <div className="fixed bottom-20 left-4 right-4 z-40 max-w-4xl mx-auto">
        <MiniPlayer
          currentTrack={currentTrack}
          isPlaying={isPlaying}
          onTogglePlay={handleTogglePlay}
          onNext={handleNext}
          onPrev={handlePrev}
          onExpandPlayer={() => setIsExpandedPlayerOpen(true)}
          onToggleFavorite={handleToggleFavorite}
        />
      </div>

      {/* Expanded Full Player Modal */}
      {isExpandedPlayerOpen && currentTrack && (
        <MusicPlayerModal
          track={currentTrack}
          isPlaying={isPlaying}
          currentSec={currentSec}
          onTogglePlay={handleTogglePlay}
          onSeek={handleSeek}
          onNext={handleNext}
          onPrev={handlePrev}
          onClose={() => setIsExpandedPlayerOpen(false)}
          onToggleFavorite={handleToggleFavorite}
          onDownload={handleDownload}
        />
      )}
    </div>
  );
}

export default App;
