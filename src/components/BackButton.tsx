import { useNavigate } from "react-router-dom";
import { IconChevronRight } from "./Icons";

export function BackButton() {
  const navigate = useNavigate();
  return (
    <button
      className="icon-btn back-btn"
      aria-label="رجوع"
      onClick={() => (window.history.length > 1 ? navigate(-1) : navigate("/"))}
    >
      <IconChevronRight size={24} />
    </button>
  );
}
