import { NavLink } from "react-router-dom";
import { useLibrary } from "../store/library";
import { IconDownloadsNav, IconHome, IconLibrary, IconSearch } from "./Icons";

export function BottomNav() {
  const { active } = useLibrary();
  const downloading = Object.values(active).filter((a) => !a.error).length;
  const items = [
    { to: "/", label: "الرئيسية", icon: <IconHome /> },
    { to: "/explore", label: "استكشاف", icon: <IconSearch /> },
    { to: "/library", label: "مكتبتي", icon: <IconLibrary /> },
    { to: "/downloads", label: "التحميلات", icon: <IconDownloadsNav />, badge: downloading || undefined },
  ];
  return (
    <nav className="bottom-nav" aria-label="التنقل الرئيسي">
      {items.map((i) => (
        <NavLink key={i.to} to={i.to} end={i.to === "/"} className="nav-item">
          <span className="nav-icon">
            {i.icon}
            {i.badge && <span className="nav-badge">{i.badge}</span>}
          </span>
          <span>{i.label}</span>
        </NavLink>
      ))}
    </nav>
  );
}
