import i18next from "i18next";
import gtag from "./gtag";
import settings from "./settings";

export const LANGUAGES = [
  { id: "en", title: "English" },
  { id: "ru", title: "Русский" },
  { id: "zh", title: "中文" },
  { id: "de", title: "Deutsch" },
  { id: "nl", title: "Nederlands" },
  { id: "he", title: "Hebrew" },
  { id: "br", title: "Brazil" },
  { id: "pl", title: "Polski" },
  { id: "ja", title: "日本語" },
  { id: "es", title: "Español" },
  { id: "kr", title: "한국어" },
  { id: "fr", title: "Français" },
  { id: "az", title: "Azərbaycanca" },
];

LANGUAGES.map((lang) => lang.id).forEach((lang) =>
  addTranslation(lang, require(`../translations/${lang}`)),
);

export function initTranslations() {
  return new Promise((resolve, reject) => {
    const language = settings.get("language");
    i18next.init(
      {
        lng: language,
        interpolation: {
          escapeValue: false,
        },
        fallbackLng: "en",
      },
      (err) => (err ? reject(err) : resolve()),
    );
    gtag("init_language", { language: language || "en" });
  });
}

export function addTranslation(lang, json) {
  i18next.on("initialized", () => {
    i18next.services.resourceStore.addResourceBundle(lang, i18next.options.ns[0], json, true, true);
  });
}

export default i18next;
