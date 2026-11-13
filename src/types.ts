export interface MediaItem {
  id: string;
  title: string;
  artist: string;
  album: string;
  durationSec: number;
  isVideo: boolean;
  artworkUrl: string;
  accentHex: string;
  streamUrl: string;
  localFilePath?: string;
  licenseBadge: string;
  isAuthorizedDownload: boolean;
  category: string;
  releaseYear: number;
  bitrateKbps: number;
  isFavorite: boolean;
  inLibrary: boolean;
  playCount: number;
  popularityScore: number;
  waveformSeed: number;
  subtitles?: { startSec: number; endSec: number; text: string }[];
}

export interface Playlist {
  id: string;
  name: string;
  description: string;
  coverUrl: string;
  accentHex: string;
  trackIds: string[];
  isFavorite: boolean;
  createdAt: number;
}

export interface DownloadTask {
  id: string;
  mediaId: string;
  title: string;
  artist: string;
  artworkUrl: string;
  format: string;
  isVideo: boolean;
  totalBytes: number;
  downloadedBytes: number;
  progressPercent: number;
  status: 'DOWNLOADING' | 'PAUSED' | 'COMPLETED' | 'FAILED';
  localFilePath: string;
  createdAt: number;
}

export interface LumoSettings {
  themeId: string;
  appLanguage: 'KM' | 'BILINGUAL' | 'EN';
  colorReactiveIntensity: 'LOW' | 'MEDIUM' | 'HIGH';
  backgroundPlayback: boolean;
  autoplay: boolean;
  defaultQuality: string;
  playbackSpeed: number;
  repeatMode: 'OFF' | 'ALL' | 'ONE';
  gaplessPlayback: boolean;
  defaultDownloadFormat: string;
  defaultDownloadQuality: string;
  wifiOnlyDownloads: boolean;
}

export interface KhmerArtistProfile {
  id: string;
  englishName: string;
  khmerName: string;
  genreBadge: string;
  aliases: string[];
  popularSongs: string[];
  imageUrl: string;
}
