import React, { useState } from 'react';
import { Heart, ListMusic, Plus, Play, Trash2 } from 'lucide-react';
import { MediaItem, Playlist } from '../types';

interface LibraryScreenProps {
  mediaItems: MediaItem[];
  playlists: Playlist[];
  onPlayTrack: (track: MediaItem) => void;
  onToggleFavorite: (trackId: string) => void;
  onCreatePlaylist: (name: string, description: string) => void;
  onOpenPlaylist: (playlist: Playlist) => void;
}

export const LibraryScreen: React.FC<LibraryScreenProps> = ({
  mediaItems,
  playlists,
  onPlayTrack,
  onToggleFavorite,
  onCreatePlaylist,
  onOpenPlaylist,
}) => {
  const [subTab, setSubTab] = useState<'favorites' | 'playlists' | 'library'>('favorites');
  const [showNewPlaylistModal, setShowNewPlaylistModal] = useState(false);
  const [newPlName, setNewPlName] = useState('');
  const [newPlDesc, setNewPlDesc] = useState('');

  const favoriteTracks = mediaItems.filter((item) => item.isFavorite);
  const libraryTracks = mediaItems.filter((item) => item.inLibrary);

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPlName.trim()) return;
    onCreatePlaylist(newPlName, newPlDesc);
    setNewPlName('');
    setNewPlDesc('');
    setShowNewPlaylistModal(false);
  };

  return (
    <div className="space-y-8 pb-24 animate-fade-in max-w-5xl mx-auto">
      {/* Tabs */}
      <div className="flex items-center justify-between border-b border-white/10 pb-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => setSubTab('favorites')}
            className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all ${subTab === 'favorites' ? 'bg-cyan-500 text-slate-950 shadow-lg' : 'bg-white/5 text-slate-300 hover:bg-white/10'}`}
          >
            Favorites ({favoriteTracks.length})
          </button>
          <button
            onClick={() => setSubTab('playlists')}
            className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all ${subTab === 'playlists' ? 'bg-cyan-500 text-slate-950 shadow-lg' : 'bg-white/5 text-slate-300 hover:bg-white/10'}`}
          >
            Playlists ({playlists.length})
          </button>
          <button
            onClick={() => setSubTab('library')}
            className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all ${subTab === 'library' ? 'bg-cyan-500 text-slate-950 shadow-lg' : 'bg-white/5 text-slate-300 hover:bg-white/10'}`}
          >
            Library ({libraryTracks.length})
          </button>
        </div>

        {subTab === 'playlists' && (
          <button
            onClick={() => setShowNewPlaylistModal(true)}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-sm shadow-lg transition-transform active:scale-95"
          >
            <Plus className="w-4 h-4" /> New Playlist
          </button>
        )}
      </div>

      {subTab === 'favorites' && (
        <div className="space-y-3">
          {favoriteTracks.length === 0 ? (
            <div className="text-center py-16 text-slate-400">
              <Heart className="w-12 h-12 mx-auto mb-3 opacity-30 text-red-500" />
              <p>No favorite tracks yet. Tap the heart icon on any song to add it here.</p>
            </div>
          ) : (
            favoriteTracks.map((track) => (
              <div
                key={track.id}
                onClick={() => onPlayTrack(track)}
                className="group cursor-pointer bg-slate-900/60 hover:bg-slate-900 border border-white/5 hover:border-cyan-500/30 rounded-2xl p-4 flex items-center justify-between gap-4 transition-all"
              >
                <div className="flex items-center gap-4 min-w-0">
                  <div className="relative w-14 h-14 rounded-xl overflow-hidden shrink-0 bg-slate-800 shadow-md">
                    <img src={track.artworkUrl} alt={track.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" referrerPolicy="no-referrer" />
                  </div>
                  <div className="min-w-0">
                    <span className="text-[10px] font-mono text-cyan-400">{track.category}</span>
                    <h4 className="text-base font-bold text-white truncate">{track.title}</h4>
                    <p className="text-xs text-slate-400 truncate">{track.artist}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <button
                    onClick={(e) => { e.stopPropagation(); onToggleFavorite(track.id); }}
                    className="p-2.5 rounded-full text-red-500 bg-white/5 hover:bg-white/10"
                  >
                    <Heart className="w-4 h-4 fill-current" />
                  </button>
                  <button className="p-3 rounded-full bg-cyan-500 text-slate-950 shadow-md">
                    <Play className="w-4 h-4 fill-current translate-x-0.5" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {subTab === 'playlists' && (
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
      )}

      {subTab === 'library' && (
        <div className="space-y-3">
          {libraryTracks.map((track) => (
            <div
              key={track.id}
              onClick={() => onPlayTrack(track)}
              className="group cursor-pointer bg-slate-900/60 hover:bg-slate-900 border border-white/5 hover:border-cyan-500/30 rounded-2xl p-4 flex items-center justify-between gap-4 transition-all"
            >
              <div className="flex items-center gap-4 min-w-0">
                <div className="relative w-14 h-14 rounded-xl overflow-hidden shrink-0 bg-slate-800 shadow-md">
                  <img src={track.artworkUrl} alt={track.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" referrerPolicy="no-referrer" />
                </div>
                <div className="min-w-0">
                  <span className="text-[10px] font-mono text-cyan-400">{track.category}</span>
                  <h4 className="text-base font-bold text-white truncate">{track.title}</h4>
                  <p className="text-xs text-slate-400 truncate">{track.artist}</p>
                </div>
              </div>
              <button className="p-3 rounded-full bg-cyan-500 text-slate-950 shadow-md">
                <Play className="w-4 h-4 fill-current translate-x-0.5" />
              </button>
            </div>
          ))}
        </div>
      )}

      {/* New Playlist Modal */}
      {showNewPlaylistModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <form onSubmit={handleCreate} className="bg-slate-900 border border-white/15 rounded-3xl p-6 w-full max-w-md space-y-6 shadow-2xl">
            <h3 className="text-xl font-extrabold text-white">Create New Playlist</h3>
            <div className="space-y-4">
              <div>
                <label className="text-xs font-mono text-slate-400 block mb-1">Playlist Name</label>
                <input
                  type="text"
                  value={newPlName}
                  onChange={(e) => setNewPlName(e.target.value)}
                  placeholder="e.g. Late Night Vibes"
                  className="w-full bg-slate-950 border border-white/10 rounded-xl px-4 py-3 text-white text-sm focus:border-cyan-500 outline-none"
                  required
                />
              </div>
              <div>
                <label className="text-xs font-mono text-slate-400 block mb-1">Description</label>
                <textarea
                  value={newPlDesc}
                  onChange={(e) => setNewPlDesc(e.target.value)}
                  placeholder="What is this playlist about?"
                  className="w-full bg-slate-950 border border-white/10 rounded-xl px-4 py-3 text-white text-sm focus:border-cyan-500 outline-none h-24 resize-none"
                />
              </div>
            </div>
            <div className="flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setShowNewPlaylistModal(false)}
                className="px-4 py-2 rounded-xl bg-white/5 hover:bg-white/10 text-slate-300 text-sm font-semibold"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 text-sm font-bold shadow-lg"
              >
                Create Playlist
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
