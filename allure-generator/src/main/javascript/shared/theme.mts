const themeVar = (name: string) => `var(${name})`;

export type Theme = "dark" | "light";

export const THEME_CHANGE_EVENT = "allure-theme-change";

const THEME_STORAGE_KEY = "allure-theme";

const isTheme = (value: string | null): value is Theme => value === "dark" || value === "light";

const getSystemThemeQuery = () =>
  typeof window.matchMedia === "function"
    ? window.matchMedia("(prefers-color-scheme: dark)")
    : null;

const readStoredTheme = (): Theme | null => {
  try {
    const value = window.localStorage.getItem(THEME_STORAGE_KEY);

    return isTheme(value) ? value : null;
  } catch {
    return null;
  }
};

const writeStoredTheme = (theme: Theme) => {
  try {
    window.localStorage.setItem(THEME_STORAGE_KEY, theme);
  } catch {
    /* Ignore storage failures in private or restricted browsing modes. */
  }
};

const resolveTheme = (): Theme => {
  const storedTheme = readStoredTheme();

  if (storedTheme) {
    return storedTheme;
  }

  return getSystemThemeQuery()?.matches ? "dark" : "light";
};

export const getActiveTheme = (): Theme =>
  document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";

const applyTheme = (theme: Theme, persist = false) => {
  const root = document.documentElement;

  if (theme === "dark") {
    root.setAttribute("data-theme", "dark");
  } else {
    root.removeAttribute("data-theme");
  }

  root.style.colorScheme = theme;

  if (persist) {
    writeStoredTheme(theme);
  }

  window.dispatchEvent(new CustomEvent(THEME_CHANGE_EVENT, { detail: { theme } }));

  return theme;
};

export const initTheme = () => {
  applyTheme(resolveTheme());

  getSystemThemeQuery()?.addEventListener("change", () => {
    if (!readStoredTheme()) {
      applyTheme(resolveTheme());
    }
  });
};

export const toggleTheme = () => {
  const nextTheme = getActiveTheme() === "dark" ? "light" : "dark";

  return applyTheme(nextTheme, true);
};

export const readThemeColor = (name: string) => {
  const probe = document.createElement("span");

  probe.style.color = `var(${name})`;
  probe.style.display = "none";
  (document.body ?? document.documentElement).appendChild(probe);

  const value = window.getComputedStyle(probe).color.trim();

  probe.remove();

  return value;
};

export const statusChartColor = (status: string) => themeVar(`--color-status-${status}-chart`);

export const chartCategoricalColors = Array.from({ length: 14 }, (_, index) =>
  themeVar(`--color-chart-categorical-${index + 1}`),
);
