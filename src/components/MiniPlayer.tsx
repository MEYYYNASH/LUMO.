import React from 'react';
import { Play, Pause, SkipForward, SkipBack, Maximize2, Heart, Volume2, Video } from 'lucide-react';
import { MediaItem } from '../types';

interface MiniPlayerProps {
  currentTrack: MediaItem | null;
  isPlaying: boolean;
  onTogglePlay: () => void;
  onNext: () => void;
  onPrev: () => void;
  onExpandPlayer: () => void;
  onToggleFavorite: (trackId: string) => void;
}

export const MiniPlayer: React.FC<MiniPlayerProps> = ({
  currentTrack,
  isPlaying,
  onTogglePlay,
  onNext,
  onPrev,
  onExpandPlayer,
  onToggleFavorite,
}) => {
  if (!currentTrack) return null;

  return (
    <div className="fixed bottom-0 left-0 right-0 z-50 bg-slate-900/95 backdrop-blur-xl border-t border-white/10 px-4 py-3 flex items-center justify-between gap-4 shadow-2xl">
      {/* Track Info */}
      <div 
        className="flex items-center gap-3.5 cursor-pointer min-w-0 flex-1"
        onClick={onExpandPlayer}
      >
        <div className="relative w-12 h-12 rounded-xl overflow-hidden shadow-md shrink-0 bg-slate-800">
          <img 
            src={currentTrack.artworkUrl} 
            alt={currentTrack.title}
            className="w-full h-full object-cover"
            referrerPolicy="no-referrer"
          />
          {currentTrack.isVideo && (
            <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
              <Video className="w-5 h-5 text-white" />
            </div>
          )}
        </div>
        <div className="min-w-0 flex-1">
          <h4 className="text-sm font-semibold text-white truncate">{currentTrack.title}</h4>
          <p className="text-xs text-slate-400 truncate">{currentTrack.artist}</p>
        </div>
      </div>

      {/* Controls */}
      <div className="flex items-center gap-4">
        <button
          onClick={(e) => { e.stopPropagation(); onToggleFavorite(currentTrack.id); }}
          className={`p-2 rounded-full transition-colors ${currentTrack.isFavorite ? 'text-red-500' : 'text-slate-400 hover:text-white'}`}
          title="Favorite"
        >
          <Heart className={`w-4 h-4 ${currentTrack.isFavorite ? 'fill-current' : ''}`} />
        </button>

        <div className="flex items-center gap-2">
          <button
            onClick={onPrev}
            className="p-2 rounded-full text-slate-300 hover:text-white hover:bg-white/10 transition-colors hidden sm:block"
            title="Previous"
          >
            <SkipBack className="w-4 h-4" />
          </button>
          <button
            onClick={onTogglePlay}
            className="p-3 rounded-full bg-cyan-500 hover:bg-cyan-400 text-slate-950 transition-transform active:scale-95 shadow-lg shadow-cyan-500/20"
            title={isPlaying ? 'Pause' : 'Play'}
          >
            {isPlaying ? <Pause className="w-5 h-5 fill-current" /> : <Play className="w-5 h-5 fill-current translate-x-0.5" />}
          </button>
          <button
            onClick={onNext}
            className="p-2 rounded-full text-slate-300 hover:text-white hover:bg-white/10 transition-colors"
            title="Next"
          >
            <SkipForward className="w-4 h-4" />
          </button>
        </div>

        <button
          onClick={onExpandPlayer}
          className="p-2 rounded-full text-slate-400 hover:text-white hover:bg-white/10 transition-colors hidden md:block"
          title="Expand Player"
        >
          <Maximize2 className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
