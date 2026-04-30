import { Activity, Dumbbell, FolderTree, Home, Menu, Users, X, ListChecks } from 'lucide-react';
import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';

const links = [
  { to: '/', label: 'Главная', icon: Home },
  { to: '/categories', label: 'Категории', icon: FolderTree },
  { to: '/exercises', label: 'Упражнения', icon: Dumbbell },
  { to: '/users', label: 'Люди', icon: Users },
  { to: '/workouts', label: 'Тренировки', icon: Activity },
  { to: '/sets', label: 'Подходы', icon: ListChecks },
];

export function Layout() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="app-shell">
      <aside className={`sidebar ${isOpen ? 'sidebar-open' : ''}`}>
        <div className="brand">
          <span className="brand-mark">GT</span>
          <div>
            <strong>GTracker</strong>
            <small>Training log</small>
          </div>
        </div>
        <nav className="nav-list">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} onClick={() => setIsOpen(false)} className={({ isActive }) => (isActive ? 'active' : '')}>
              <Icon size={20} />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main-shell">
        <header className="topbar">
          <button type="button" className="icon-button menu-button" onClick={() => setIsOpen((value) => !value)} aria-label="Открыть меню">
            {isOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
          <div>
            <strong>GTracker</strong>
            <span>Тренировки без путаницы</span>
          </div>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
