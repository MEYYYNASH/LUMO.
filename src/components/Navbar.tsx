import React from 'react';
import { Search, Library, Download, Settings, Home, Disc, Sparkles } from 'lucide-react';

interface NavbarProps {
  currentTab: string;
  onTabChange: (tab: string) => void;
  activeThemeName: string;
  onOpenThemeModal: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTab,
  onTabChange,
  activeThemeName,
  onOpenThemeModal,
}) => {
  return (
    <header className="sticky top-0 z-40 w-full backdrop-blur-xl bg-slate-950/80 border-b border-white/10 px-6 py-4 flex items-center justify-between gap-8">
      {/* Zone 1: Brand Wordmark */}
      <div 
        onClick={() => onTabChange('home')}
        className="flex items-center gap-3 cursor-pointer whitespace-nowrap shrink-0 group"
      >
        <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20 group-hover:scale-105 transition-transform">
          <Disc className="w-5 h-5 text-slate-950 animate-spin-slow" />
        </div>
        <div className="flex flex-col">
          <span className="text-lg font-extrabold tracking-wider text-white">LUMO</span>
          <span className="text-[10px] uppercase tracking-widest text-cyan-400 font-mono">Hi-Fi Audio</span>
        </div>
      </div>

      {/* Zone 2: Navigation Links */}
      <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-slate-400">
        <button 
          onClick={() => onTabChange('home')}
          className={`flex items-center gap-2 transition-colors whitespace-nowrap shrink-0 ${currentTab === 'home' ? 'text-white font-semibold' : 'hover:text-slate-200'}`}
        >
          <Home className="w-4 h-4" /> Home
        </button>
        <button 
          onClick={() => onTabChange('search')}
          className={`flex items-center gap-2 transition-colors whitespace-nowrap shrink-0 ${currentTab === 'search' ? 'text-white font-semibold' : 'hover:text-slate-200'}`}
        >
          <Search className="w-4 h-4" /> Explore & Search
        </button>
        <button 
          onClick={() => onTabChange('library')}
          className={`flex items-center gap-2 transition-colors whitespace-nowrap shrink-0 ${currentTab === 'library' ? 'text-white font-semibold' : 'hover:text-slate-200'}`}
        >
          <Library className="w-4 h-4" /> Library & Playlists
        </button>
        <button 
          onClick={() => onTabChange('downloads')}
          className={`flex items-center gap-2 transition-colors whitespace-nowrap shrink-0 ${currentTab === 'downloads' ? 'text-white font-semibold' : 'hover:text-slate-200'}`}
        >
          <Download className="w-4 h-4" /> Downloads
        </button>
      </nav>

      {/* Zone 3: Primary Action / Theme Selector */}
      <div className="flex items-center gap-3 shrink-0">
        <button
          onClick={onOpenThemeModal}
          className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-white/5 border border-white/10 hover:bg-white/10 text-xs text-slate-200 transition-colors whitespace-nowrap"
        >
          <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
          <span className="hidden sm:inline">{activeThemeName}</span>
        </button>
        <button
          onClick={() => onTabChange('settings')}
          className="p-2 rounded-lg bg-white/5 border border-white/10 hover:bg-white/10 text-slate-300 transition-colors"
          title="Settings"
        >
          <Settings className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
};
