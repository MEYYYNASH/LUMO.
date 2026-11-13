import React, { useState, useRef } from 'react';
import { Home, Search, Library, Settings } from 'lucide-react';

interface LiquidGlassNavProps {
  currentTab: string;
  onTabChange: (tab: string) => void;
}

export const LiquidGlassNav: React.FC<LiquidGlassNavProps> = ({
  currentTab,
  onTabChange,
}) => {
  const [isHolding, setIsHolding] = useState(false);
  const [dragOffset, setDragOffset] = useState(0);
  const [hoveredTab, setHoveredTab] = useState<string | null>(null);
  const holdTimerRef = useRef<any>(null);
  const navRef = useRef<HTMLDivElement>(null);

  const tabs = [
    { id: 'home', label: 'Home', icon: Home },
    { id: 'search', label: 'Search', icon: Search },
    { id: 'library', label: 'Library', icon: Library },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  const activeIndex = tabs.findIndex((t) => t.id === currentTab);

  const handleTouchStartOrMouseDown = (tabId: string) => {
    // 170ms hold activation for glass lens mode
    holdTimerRef.current = setTimeout(() => {
      setIsHolding(true);
    }, 170);
  };

  const handleTouchEndOrMouseUp = (tabId: string) => {
    if (holdTimerRef.current) {
      clearTimeout(holdTimerRef.current);
      holdTimerRef.current = null;
    }
    setIsHolding(false);
    setDragOffset(0);
    onTabChange(tabId);
  };

  const handleMouseMoveOrTouchMove = (e: React.PointerEvent) => {
    if (!isHolding || !navRef.current) return;
    const rect = navRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const tabWidth = rect.width / tabs.length;
    const calculatedIndex = Math.floor(x / tabWidth);
    if (calculatedIndex >= 0 && calculatedIndex < tabs.length) {
      setHoveredTab(tabs[calculatedIndex].id);
    }
  };

  return (
    <div className="fixed bottom-6 left-0 right-0 z-50 flex justify-center px-4 pointer-events-none">
      <div
        ref={navRef}
        onPointerMove={handleMouseMoveOrTouchMove}
        onPointerUp={() => {
          if (isHolding && hoveredTab) {
            onTabChange(hoveredTab);
          }
          setIsHolding(false);
          setHoveredTab(null);
        }}
        className="glass-nav relative flex items-center justify-around w-full max-w-md px-3 py-2.5 pointer-events-auto shadow-2xl transition-all duration-300 select-none"
      >
        {tabs.map((tab, idx) => {
          const Icon = tab.icon;
          const isActive = currentTab === tab.id;
          const isHoveredOrActive = (isHolding && hoveredTab === tab.id) || isActive;

          return (
            <div
              key={tab.id}
              onMouseDown={() => handleTouchStartOrMouseDown(tab.id)}
              onMouseUp={() => handleTouchEndOrMouseUp(tab.id)}
              onTouchStart={() => handleTouchStartOrMouseDown(tab.id)}
              onTouchEnd={() => handleTouchEndOrMouseUp(tab.id)}
              className="relative flex flex-col items-center justify-center py-2 px-5 cursor-pointer group"
            >
              {/* Floating Active Glass Chip with optical lens & blue/pink reflections */}
              {isHoveredOrActive && (
                <div 
                  className={`absolute inset-0 glass-chip transition-all duration-300 ${isHolding ? 'scale-125 -inset-y-1 opacity-90' : 'scale-100'}`}
                />
              )}

              <div className={`relative z-10 flex items-center justify-center transition-transform duration-300 ${isHoveredOrActive ? 'scale-118 text-[#007AFF]' : 'text-slate-400 group-hover:text-slate-200'}`}>
                <Icon className={`w-6 h-6 ${isHoveredOrActive ? 'active-tab-icon' : ''}`} />
              </div>
              <span className={`relative z-10 text-[10px] font-semibold mt-1 tracking-tight transition-colors ${isHoveredOrActive ? 'text-[#007AFF]' : 'text-slate-400 group-hover:text-slate-200'}`}>
                {tab.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};
