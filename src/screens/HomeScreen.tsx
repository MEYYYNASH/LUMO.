import React from 'react';
import { Play, Sparkles, TrendingUp, ShieldCheck, Music, Video, Heart, Download } from 'lucide-react';
import { MediaItem, Playlist, KhmerArtistProfile } from '../types';
import { KHMER_ARTISTS_CATALOG } from '../data/lumoData';

interface HomeScreenProps {
  mediaItems: MediaItem[];
  playlists: Playlist[];
  onPlayTrack: (track: MediaItem) => void;
  onSelectArtist: (artist: KhmerArtistProfile) => void;
  onToggleFavorite: (trackId: string) => void;
  onOpenPlaylist: (playlist: Playlist) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  mediaItems,
  playlists,
  onPlayTrack,
  onSelectArtist,
  onToggleFavorite,
  onOpenPlaylist,
}) => {
  const featuredTrack = mediaItems[0] || mediaItems[1];

  return (
    <div className="space-y-12 pb-24 animate-fade-in">
      {/* Hero Banner */}
      {featuredTrack && (
        <div className="relative rounded-3xl overflow-hidden border border-white/10 bg-gradient-to-r from-slate-900 via-slate-900/90 to-cyan-950/40 p-8 sm:p-12 shadow-2xl">
          <div className="absolute right-0 top-0 bottom-0 w-1/2 opacity-30 sm:opacity-50 pointer-events-none hidden md:block">
            <img 
              src={featuredTrack.artworkUrl} 
              alt={featuredTrack.title}
              className="w-full h-full object-cover mask-radial"
              referrerPolicy="no-referrer"
            />
          </div>
          <div className="relative z-10 max-w-xl space-y-4">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-mono">
              <Sparkles className="w-3.5 h-3.5" /> {featuredTrack.licenseBadge}
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
              {featuredTrack.title}
            </h1>
            <p className="text-slate-300 text-sm sm:text-base font-medium">
              {featuredTrack.artist} · {featuredTrack.album}
            </p>
            <div className="flex items-center gap-4 pt-2">
              <button
                onClick={() => onPlayTrack(featuredTrack)}
                className="flex items-center gap-2 px-6 py-3 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold transition-transform active:scale-95 shadow-lg shadow-cyan-500/25"
              >
                <Play className="w-5 h-5 fill-current" /> Play Hi-Fi Audio
              </button>
              <button
                onClick={() => onToggleFavorite(featuredTrack.id)}
                className={`p-3 rounded-xl bg-white/5 border border-white/10 hover:bg-white/10 transition-colors ${featuredTrack.isFavorite ? 'text-red-500' : 'text-slate-300'}`}
              >
                <Heart className={`w-5 h-5 ${featuredTrack.isFavorite ? 'fill-current' : ''}`} />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Khmer Artist Directory */}
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight">Khmer Artist Directory</h2>
            <p className="text-xs text-slate-400 font-mono mt-0.5">Cambodia's legendary & modern music icons</p>
          </div>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-4">
          {KHMER_ARTISTS_CATALOG.map((artist) => (
            <div
              key={artist.id}
              onClick={() => onSelectArtist(artist)}
              className="group cursor-pointer bg-slate-900/60 border border-white/5 hover:border-cyan-500/30 rounded-2xl p-4 flex flex-col items-center text-center transition-all hover:scale-105"
            >
              <div className="w-20 h-20 rounded-full overflow-hidden mb-3 border-2 border-white/10 group-hover:border-cyan-400 transition-colors shadow-md">
                <img src={artist.imageUrl} alt={artist.englishName} className="w-full h-full object-cover" referrerPolicy="no-referrer" />
              </div>
              <h3 className="text-sm font-bold text-white truncate w-full">{artist.englishName}</h3>
              <p className="text-xs text-cyan-400 font-mono mt-0.5">{artist.khmerName}</p>
              <span className="text-[10px] text-slate-400 mt-1 truncate w-full">{artist.genreBadge}</span>
            </div>
          ))}
        </div>
      </section>

      {/* Trending 2026 Charts */}
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight">Trending 2026 Charts</h2>
            <p className="text-xs text-slate-400 font-mono mt-0.5">Global & local hits streamed worldwide</p>
          </div>
          <div className="flex items-center gap-1.5 text-xs text-cyan-400 font-mono">
            <TrendingUp className="w-4 h-4" /> Live Global Play Counts
          </div>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {mediaItems.map((track, idx) => (
            <div
              key={track.id}
              onClick={() => onPlayTrack(track)}
              className="group cursor-pointer bg-slate-900/40 hover:bg-slate-900 border border-white/5 hover:border-white/15 rounded-2xl p-4 flex items-center gap-4 transition-all"
            >
              <div className="relative w-16 h-16 rounded-xl overflow-hidden shrink-0 bg-slate-800 shadow-md">
                <img src={track.artworkUrl} alt={track.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" referrerPolicy="no-referrer" />
                <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                  <Play className="w-6 h-6 text-white fill-current" />
                </div>
              </div>
              <div className="min-w-0 flex-1">
                <span className="text-[10px] font-mono text-cyan-400">🌍 #{idx + 1} · {track.category}</span>
                <h4 className="text-sm font-bold text-white truncate mt-0.5">{track.title}</h4>
                <p className="text-xs text-slate-400 truncate">{track.artist}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Curated Playlists */}
      <section className="space-y-6">
        <div>
          <h2 className="text-xl font-bold text-white tracking-tight">Curated Playlists</h2>
          <p className="text-xs text-slate-400 font-mono mt-0.5">Hand-crafted musical journeys</p>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          {playlists.map((pl) => (
            <div
              key={pl.id}
              onClick={() => onOpenPlaylist(pl)}
              className="cursor-pointer bg-gradient-to-br from-slate-900 to-slate-950 border border-white/10 hover:border-cyan-500/30 rounded-2xl p-6 flex items-center gap-6 transition-all group shadow-lg"
            >
              <div className="w-24 h-24 rounded-2xl overflow-hidden shadow-md shrink-0">
                <img src={pl.coverUrl} alt={pl.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform" referrerPolicy="no-referrer" />
              </div>
              <div className="space-y-2 min-w-0 flex-1">
                <span className="text-xs font-mono text-cyan-400">{pl.trackIds.length} Tracks</span>
                <h3 className="text-lg font-bold text-white truncate">{pl.name}</h3>
                <p className="text-xs text-slate-400 line-clamp-2">{pl.description}</p>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};
