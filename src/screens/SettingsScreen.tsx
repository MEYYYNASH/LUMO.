import React from 'react';
import { Sparkles, Globe, Volume2, ShieldCheck, HardDrive, Smartphone } from 'lucide-react';
import { LumoSettings } from '../types';
import { LUMO_THEMES } from '../data/lumoData';

interface SettingsScreenProps {
  settings: LumoSettings;
  onUpdateSettings: (newSettings: Partial<LumoSettings>) => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  settings,
  onUpdateSettings,
}) => {
  return (
    <div className="space-y-8 pb-24 animate-fade-in max-w-4xl mx-auto">
      <div>
        <h2 className="text-2xl font-extrabold text-white">LUMO Settings</h2>
        <p className="text-xs text-slate-400 font-mono mt-1">Customize themes, audio quality, and playback preferences</p>
      </div>

      {/* Theme Section */}
      <div className="bg-slate-900/60 border border-white/10 rounded-3xl p-6 space-y-4 shadow-xl">
        <h3 className="text-lg font-bold text-white flex items-center gap-2">
          <Sparkles className="w-5 h-5 text-cyan-400" /> Curated Themes
        </h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {LUMO_THEMES.map((th) => (
            <button
              key={th.id}
              onClick={() => onUpdateSettings({ themeId: th.id })}
              className={`p-4 rounded-2xl border text-left flex flex-col gap-2 transition-all ${settings.themeId === th.id ? 'border-cyan-400 bg-cyan-500/10 shadow-lg' : 'border-white/10 bg-slate-950/40 hover:bg-slate-950'}`}
            >
              <div className="flex items-center justify-between">
                <div className="w-6 h-6 rounded-full shadow-inner" style={{ backgroundColor: th.primary }} />
                {settings.themeId === th.id && <span className="text-[10px] font-mono text-cyan-400 font-bold">ACTIVE</span>}
              </div>
              <span className="text-sm font-bold text-white">{th.name}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Language Section */}
      <div className="bg-slate-900/60 border border-white/10 rounded-3xl p-6 space-y-4 shadow-xl">
        <h3 className="text-lg font-bold text-white flex items-center gap-2">
          <Globe className="w-5 h-5 text-cyan-400" /> Language Display
        </h3>
        <div className="grid grid-cols-3 gap-4">
          {[
            { id: 'KM', label: 'ភាសាខ្មែរ (Khmer)' },
            { id: 'BILINGUAL', label: 'Bilingual (EN / KM)' },
            { id: 'EN', label: 'English Only' },
          ].map((lang) => (
            <button
              key={lang.id}
              onClick={() => onUpdateSettings({ appLanguage: lang.id as any })}
              className={`p-4 rounded-2xl border text-sm font-semibold transition-all ${settings.appLanguage === lang.id ? 'border-cyan-400 bg-cyan-500/10 text-cyan-400' : 'border-white/10 bg-slate-950/40 text-slate-300 hover:bg-slate-950'}`}
            >
              {lang.label}
            </button>
          ))}
        </div>
      </div>

      {/* Audio Quality */}
      <div className="bg-slate-900/60 border border-white/10 rounded-3xl p-6 space-y-4 shadow-xl">
        <h3 className="text-lg font-bold text-white flex items-center gap-2">
          <Volume2 className="w-5 h-5 text-cyan-400" /> Hi-Fi Audio Quality
        </h3>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {['High 320 kbps AAC', 'Lossless FLAC', 'Standard 192 kbps'].map((q) => (
            <button
              key={q}
              onClick={() => onUpdateSettings({ defaultQuality: q })}
              className={`p-4 rounded-2xl border text-sm font-semibold transition-all ${settings.defaultQuality === q ? 'border-cyan-400 bg-cyan-500/10 text-cyan-400' : 'border-white/10 bg-slate-950/40 text-slate-300 hover:bg-slate-950'}`}
            >
              {q}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
