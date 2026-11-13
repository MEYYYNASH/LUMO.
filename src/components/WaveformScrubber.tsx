import React from 'react';

interface WaveformScrubberProps {
  durationSec: number;
  currentSec: number;
  onSeek: (seconds: number) => void;
  seed?: number;
  accentColor?: string;
}

export const WaveformScrubber: React.FC<WaveformScrubberProps> = ({
  durationSec,
  currentSec,
  onSeek,
  seed = 42,
  accentColor = '#3B82F6'
}) => {
  const barsCount = 48;
  const progressPercent = durationSec > 0 ? (currentSec / durationSec) * 100 : 0;

  // Generate deterministic pseudo-random waveform heights
  const bars = Array.from({ length: barsCount }).map((_, i) => {
    const phase = (i + 1) * 0.43 + seed * 0.71;
    const height = 0.2 + Math.abs(Math.sin(phase)) * 0.8;
    return height;
  });

  const handleClick = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const width = rect.width;
    const ratio = Math.max(0, Math.min(1, clickX / width));
    onSeek(Math.floor(ratio * durationSec));
  };

  const formatTime = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="w-full flex flex-col gap-1.5 select-none">
      <div 
        className="relative h-12 flex items-center gap-[3px] cursor-pointer group py-2"
        onClick={handleClick}
      >
        {bars.map((height, i) => {
          const barPercent = (i / barsCount) * 100;
          const isPlayed = barPercent <= progressPercent;
          return (
            <div
              key={i}
              className="flex-1 rounded-full transition-all duration-150 group-hover:opacity-90"
              style={{
                height: `${Math.max(15, height * 100)}%`,
                backgroundColor: isPlayed ? accentColor : 'rgba(255, 255, 255, 0.2)'
              }}
            />
          );
        })}
      </div>
      <div className="flex justify-between text-xs text-slate-400 font-mono">
        <span>{formatTime(currentSec)}</span>
        <span>{formatTime(durationSec)}</span>
      </div>
    </div>
  );
};
