import React from 'react';
import { ArrowLeft, Play, Heart, Sparkles } from 'lucide-react';
import { KhmerArtistProfile, MediaItem } from '../types';

interface ArtistDetailScreenProps {
  artist: KhmerArtistProfile;
  mediaItems: MediaItem[];
  onBack: () => void;
  onPlayTrack: (track: MediaItem) => void;
  onToggleFavorite: (trackId: string) => void;
}

export const ArtistDetailScreen: React.FC<ArtistDetailScreenProps> = ({
  artist,
  mediaItems,
  onBack,
  onPlayTrack,
  onToggleFavorite,
}) => {
  const artistTracks = mediaItems.filter(
    (item) => item.artist.toLowerCase().includes(artist.englishName.toLowerCase()) || item.artist.includes(artist.khmerName)
  );

  return (
    <div className="space-y-8 pb-24 animate-fade-in max-w-4xl mx-auto">
      <button
        onClick={onBack}
        className="flex items-center gap-2 px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-sm font-semibold transition-colors"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Home
      </button>

      {/* Artist Hero Header */}
      <div className="relative rounded-3xl overflow-hidden bg-gradient-to-br from-slate-900 to-slate-950 border border-white/10 p-8 flex flex-col sm:flex-row items-center gap-6 shadow-2xl">
        <div className="w-32 h-32 sm:w-40 sm:h-40 rounded-full overflow-hidden border-4 border-white/15 shadow-xl shrink-0">
          <img src={artist.imageUrl} alt={artist.englishName} className="w-full h-full object-cover" referrerPolicy="no-referrer" />
        </div>
        <div className="space-y-3 text-center sm:text-left">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-mono">
            <Sparkles className="w-3.5 h-3.5" /> {artist.genreBadge}
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold text-white">{artist.englishName}</h1>
          <p className="text-xl text-cyan-400 font-bold">{artist.khmerName}</p>
          <div className="flex flex-wrap gap-2 justify-center sm:justify-start pt-2">
            {artist.popularSongs.map((song, i) => (
              <span key={i} className="text-xs px-3 py-1 rounded-full bg-white/5 border border-white/10 text-slate-300 font-mono">
                {song}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Track List */}
      <div className="space-y-4">
        <h3 className="text-lg font-bold text-white tracking-tight">Popular Tracks & Releases</h3>
        <div className="space-y-3">
          {artistTracks.length === 0 ? (
            <div className="text-center py-12 text-slate-400 bg-slate-900/40 border border-white/5 rounded-2xl">
              <p>Explore authorized catalog tracks for {artist.englishName}.</p>
            </div>
          ) : (
            artistTracks.map((track) => (
              <div
                key={track.id}
                onClick={() => onPlayTrack(track)}
                className="group cursor-pointer bg-slate-900/60 hover:bg-slate-900 border border-white/5 hover:border-cyan-500/30 rounded-2xl p-4 flex items-center justify-between gap-4 transition-all"
              >
                <div className="flex items-center gap-4 min-w-0">
                  <div className="w-14 h-14 rounded-xl overflow-hidden shrink-0 bg-slate-800 shadow-md">
                    <img src={track.artworkUrl} alt={track.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" referrerPolicy="no-referrer" />
                  </div>
                  <div className="min-w-0">
                    <span className="text-[10px] font-mono text-cyan-400">{track.category} · {track.releaseYear}</span>
                    <h4 className="text-base font-bold text-white truncate">{track.title}</h4>
                    <p className="text-xs text-slate-400 truncate">{track.album}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <button
                    onClick={(e) => { e.stopPropagation(); onToggleFavorite(track.id); }}
                    className={`p-2.5 rounded-full bg-white/5 hover:bg-white/10 transition-colors ${track.isFavorite ? 'text-red-500' : 'text-slate-400'}`}
                  >
                    <Heart className={`w-4 h-4 ${track.isFavorite ? 'fill-current' : ''}`} />
                  </button>
                  <button className="p-3 rounded-full bg-cyan-500 text-slate-950 shadow-md">
                    <Play className="w-4 h-4 fill-current translate-x-0.5" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
