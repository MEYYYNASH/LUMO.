import React from 'react';
import { Download, CheckCircle2, HardDrive, ShieldCheck, Play } from 'lucide-react';
import { DownloadTask, MediaItem } from '../types';

interface DownloadsScreenProps {
  downloads: DownloadTask[];
  onPlayTrack: (track: MediaItem) => void;
  mediaItems: MediaItem[];
}

export const DownloadsScreen: React.FC<DownloadsScreenProps> = ({
  downloads,
  onPlayTrack,
  mediaItems,
}) => {
  return (
    <div className="space-y-8 pb-24 animate-fade-in max-w-4xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-extrabold text-white">Authorized Media Downloads</h2>
          <p className="text-xs text-slate-400 font-mono mt-1">High-fidelity offline storage & cache manager</p>
        </div>
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-mono">
          <HardDrive className="w-4 h-4" /> 1.2 GB Available
        </div>
      </div>

      <div className="space-y-4">
        {downloads.length === 0 ? (
          <div className="text-center py-16 bg-slate-900/40 border border-white/5 rounded-3xl p-8">
            <Download className="w-12 h-12 mx-auto mb-3 opacity-30 text-cyan-400" />
            <h3 className="text-lg font-bold text-white mb-1">No Active Downloads</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              Tap the download icon on any track or video to save authorized offline media.
            </p>
          </div>
        ) : (
          downloads.map((task) => {
            const track = mediaItems.find((m) => m.id === task.mediaId);
            return (
              <div
                key={task.id}
                className="bg-slate-900/60 border border-white/10 rounded-2xl p-4 flex items-center justify-between gap-4 shadow-lg"
              >
                <div className="flex items-center gap-4 min-w-0">
                  <div className="w-14 h-14 rounded-xl overflow-hidden shrink-0 bg-slate-800">
                    <img src={task.artworkUrl} alt={task.title} className="w-full h-full object-cover" referrerPolicy="no-referrer" />
                  </div>
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-mono text-cyan-400">{task.format}</span>
                      <span className="text-[10px] font-mono text-emerald-400 flex items-center gap-1">
                        <CheckCircle2 className="w-3 h-3" /> Downloaded
                      </span>
                    </div>
                    <h4 className="text-sm font-bold text-white truncate mt-0.5">{task.title}</h4>
                    <p className="text-xs text-slate-400 truncate">{task.artist}</p>
                  </div>
                </div>

                {track && (
                  <button
                    onClick={() => onPlayTrack(track)}
                    className="p-3 rounded-full bg-cyan-500 hover:bg-cyan-400 text-slate-950 transition-transform active:scale-95 shadow-md shrink-0"
                  >
                    <Play className="w-4 h-4 fill-current translate-x-0.5" />
                  </button>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
