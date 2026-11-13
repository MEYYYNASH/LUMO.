import React, { useState } from 'react';
import { Search as SearchIcon, Play, Heart, Video } from 'lucide-react';
import { MediaItem } from '../types';

interface SearchScreenProps {
  mediaItems: MediaItem[];
  onPlayTrack: (track: MediaItem) => void;
  onToggleFavorite: (trackId: string) => void;
}

export const SearchScreen: React.FC<SearchScreenProps> = ({
  mediaItems,
  onPlayTrack,
  onToggleFavorite,
}) => {
  const [query, setQuery] = useState('');

  const filteredItems = mediaItems.filter((item) => {
    if (!query.trim()) return true;
    const q = query.toLowerCase();
    return (
      item.title.toLowerCase().includes(q) ||
      item.artist.toLowerCase().includes(q) ||
      item.album.toLowerCase().includes(q) ||
      item.category.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-8 pb-24 animate-fade-in">
      {/* Search Input */}
      <div className="relative max-w-2xl mx-auto">
        <SearchIcon className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search Khmer artists, songs, albums, or videos (e.g. VannDa, Sina, Time To Rise)..."
          className="w-full bg-slate-900 border border-white/15 focus:border-cyan-500 rounded-2xl pl-12 pr-4 py-4 text-sm text-white placeholder-slate-500 outline-none shadow-xl transition-colors"
        />
      </div>

      {/* Results */}
      <div className="space-y-4 max-w-4xl mx-auto">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-bold text-slate-400 uppercase tracking-widest font-mono">
            {filteredItems.length} Results Found
          </h3>
        </div>

        <div className="space-y-3">
          {filteredItems.map((track) => (
            <div
              key={track.id}
              onClick={() => onPlayTrack(track)}
              className="group cursor-pointer bg-slate-900/60 hover:bg-slate-900 border border-white/5 hover:border-cyan-500/30 rounded-2xl p-4 flex items-center justify-between gap-4 transition-all"
            >
              <div className="flex items-center gap-4 min-w-0">
                <div className="relative w-14 h-14 rounded-xl overflow-hidden shrink-0 bg-slate-800 shadow-md">
                  <img src={track.artworkUrl} alt={track.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" referrerPolicy="no-referrer" />
                  {track.isVideo && (
                    <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                      <Video className="w-5 h-5 text-white" />
                    </div>
                  )}
                </div>
                <div className="min-w-0">
                  <span className="text-[10px] font-mono text-cyan-400">{track.category} · {track.releaseYear}</span>
                  <h4 className="text-base font-bold text-white truncate">{track.title}</h4>
                  <p className="text-xs text-slate-400 truncate">{track.artist} · {track.album}</p>
                </div>
              </div>

              <div className="flex items-center gap-3 shrink-0">
                <button
                  onClick={(e) => { e.stopPropagation(); onToggleFavorite(track.id); }}
                  className={`p-2.5 rounded-full bg-white/5 hover:bg-white/10 transition-colors ${track.isFavorite ? 'text-red-500' : 'text-slate-400'}`}
                >
                  <Heart className={`w-4 h-4 ${track.isFavorite ? 'fill-current' : ''}`} />
                </button>
                <button className="p-3 rounded-full bg-cyan-500 hover:bg-cyan-400 text-slate-950 transition-transform active:scale-95 shadow-md">
                  <Play className="w-4 h-4 fill-current translate-x-0.5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
