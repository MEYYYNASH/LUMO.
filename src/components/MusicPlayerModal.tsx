import React, { useState } from 'react';
import { X, Play, Pause, SkipForward, SkipBack, Heart, Download, Share2, Sparkles, Volume2, Repeat, Shuffle, Video, ListMusic } from 'lucide-react';
import { MediaItem } from '../types';
import { WaveformScrubber } from './WaveformScrubber';

interface MusicPlayerModalProps {
  track: MediaItem;
  isPlaying: boolean;
  currentSec: number;
  onTogglePlay: () => void;
  onSeek: (seconds: number) => void;
  onNext: () => void;
  onPrev: () => void;
  onClose: () => void;
  onToggleFavorite: (trackId: string) => void;
  onDownload: (track: MediaItem) => void;
}

export const MusicPlayerModal: React.FC<MusicPlayerModalProps> = ({
  track,
  isPlaying,
  currentSec,
  onTogglePlay,
  onSeek,
  onNext,
  onPrev,
  onClose,
  onToggleFavorite,
  onDownload,
}) => {
  const [activeTab, setActiveTab] = useState<'cover' | 'lyrics'>('cover');

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/90 backdrop-blur-2xl flex flex-col overflow-y-auto animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between px-6 py-4 border-b border-white/15">
        <div className="flex items-center gap-3">
          <div className="w-2.5 h-2.5 rounded-full bg-cyan-400 animate-pulse" />
          <span className="text-xs uppercase tracking-widest text-cyan-400 font-mono">Now Playing · LUMO Hi-Fi</span>
        </div>
        <button
          onClick={onClose}
          className="p-2.5 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors"
          title="Close"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Main Content */}
      <div className="flex-1 max-w-4xl mx-auto w-full px-6 py-8 flex flex-col items-center justify-center gap-8">
        {/* Toggle View Tabs */}
        <div className="flex items-center gap-2 p-1 bg-white/5 rounded-xl border border-white/10">
          <button
            onClick={() => setActiveTab('cover')}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all ${activeTab === 'cover' ? 'bg-cyan-500 text-slate-950 shadow-lg' : 'text-slate-400 hover:text-white'}`}
          >
            Artwork & Waveform
          </button>
          <button
            onClick={() => setActiveTab('lyrics')}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all ${activeTab === 'lyrics' ? 'bg-cyan-500 text-slate-950 shadow-lg' : 'text-slate-400 hover:text-white'}`}
          >
            Live Subtitles / Lyrics
          </button>
        </div>

        {activeTab === 'cover' ? (
          <div className="flex flex-col items-center gap-8 w-full max-w-md">
            {/* Album Artwork with reactive glow */}
            <div className="relative group w-72 h-72 sm:w-80 sm:h-80 rounded-3xl overflow-hidden shadow-2xl border border-white/20">
              <div 
                className="absolute inset-0 opacity-40 blur-3xl -z-10 transition-transform duration-500"
                style={{ backgroundColor: track.accentHex }}
              />
              <img 
                src={track.artworkUrl} 
                alt={track.title} 
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                referrerPolicy="no-referrer"
              />
              {track.isVideo && (
                <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                  <Video className="w-12 h-12 text-white animate-bounce" />
                </div>
              )}
            </div>

            {/* Track Metadata */}
            <div className="text-center w-full">
              <div className="inline-block px-3 py-1 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-mono mb-3">
                {track.licenseBadge}
              </div>
              <h2 className="text-2xl sm:text-3xl font-extrabold text-white mb-2">{track.title}</h2>
              <p className="text-base text-slate-400 font-medium">{track.artist}</p>
              <p className="text-xs text-slate-500 mt-1">{track.album} · {track.releaseYear}</p>
            </div>
          </div>
        ) : (
          <div className="w-full max-w-xl bg-slate-900/80 backdrop-blur-md rounded-2xl border border-white/10 p-6 flex flex-col gap-4 min-h-[320px] justify-center items-center text-center">
            {track.subtitles && track.subtitles.length > 0 ? (
              track.subtitles.map((cue, idx) => {
                const isActive = currentSec >= cue.startSec && currentSec <= cue.endSec;
                return (
                  <div 
                    key={idx} 
                    className={`transition-all duration-300 py-2 ${isActive ? 'text-cyan-400 text-lg font-bold scale-105' : 'text-slate-400 text-sm opacity-70'}`}
                  >
                    {cue.text}
                  </div>
                );
              })
            ) : (
              <div className="text-slate-400 flex flex-col items-center gap-2">
                <Sparkles className="w-8 h-8 text-cyan-400 animate-spin-slow" />
                <p>Authorized streaming lyrics & subtitles ready for {track.title}</p>
              </div>
            )}
          </div>
        )}

        {/* Waveform Scrubber & Controls */}
        <div className="w-full max-w-xl flex flex-col gap-6">
          <WaveformScrubber 
            durationSec={track.durationSec}
            currentSec={currentSec}
            onSeek={onSeek}
            seed={track.waveformSeed}
            accentColor={track.accentHex}
          />

          <div className="flex items-center justify-between px-4">
            <button 
              onClick={() => onToggleFavorite(track.id)}
              className={`p-3 rounded-full bg-white/5 border border-white/10 hover:bg-white/10 transition-colors ${track.isFavorite ? 'text-red-500' : 'text-slate-300'}`}
            >
              <Heart className={`w-5 h-5 ${track.isFavorite ? 'fill-current' : ''}`} />
            </button>

            <div className="flex items-center gap-6">
              <button onClick={onPrev} className="p-3 rounded-full text-slate-300 hover:text-white hover:bg-white/10 transition-colors">
                <SkipBack className="w-6 h-6" />
              </button>
              <button 
                onClick={onTogglePlay}
                className="p-5 rounded-full bg-gradient-to-r from-cyan-500 to-blue-600 text-slate-950 shadow-xl shadow-cyan-500/25 hover:scale-105 transition-transform"
              >
                {isPlaying ? <Pause className="w-7 h-7 fill-current" /> : <Play className="w-7 h-7 fill-current translate-x-0.5" />}
              </button>
              <button onClick={onNext} className="p-3 rounded-full text-slate-300 hover:text-white hover:bg-white/10 transition-colors">
                <SkipForward className="w-6 h-6" />
              </button>
            </div>

            <button 
              onClick={() => onDownload(track)}
              className="p-3 rounded-full bg-white/5 border border-white/10 hover:bg-white/10 text-cyan-400 transition-colors"
              title="Download Authorized Media"
            >
              <Download className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
