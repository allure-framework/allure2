import i18next from "i18next";
import gtag from "./gtag";
import settings from "./settings";

export const LANGUAGES = [
  { id: "am", title: "Հայերեն" },
  { id: "az", title: "Azərbaycanca" },
  { id: "br", title: "Brazil" },
  { id: "de", title: "Deutsch" },
  { id: "en", title: "English" },
  { id: "es", title: "Español" },
  { id: "fr", title: "Français" },
  { id: "he", title: "Hebrew" },
  { id: "hu", title: "Magyar" },
  { id: "isv", abbr: "Ⱄ", title: "Medžuslovjansky" },
  { id: "it", title: "Italiano" },
  { id: "ja", title: "日本語" },
  { id: "ka", title: "ქართული" },
  { id: "kr", title: "한국어" },
  { id: "nl", title: "Nederlands" },
  { id: "pl", title: "Polski" },
  { id: "ru", title: "Русский" },
  { id: "sv", title: "Svenska" },
  { id: "tr", title: "Türkçe" },
  { id: "zh", title: "中文" },
];

LANGUAGES.map((lang) => lang.id).forEach((lang) =>
  addTranslation(lang, require(`../translations/${lang}`)),
);

export function initTranslations() {
  return new Promise((resolve, reject) => {
    const language = settings.getLanguage();
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

    i18next.on("initialized", () => {
      const pluralResolver = i18next.services.pluralResolver;
      pluralResolver.addRule("isv", pluralResolver.getRule("be"));
    });

    gtag("init_language", { language: language || "en" });
  });
}

export function addTranslation(lang, json) {
  i18next.on("initialized", () => {
    i18next.services.resourceStore.addResourceBundle(lang, i18next.options.ns[0], json, true, true);
  });
}

export default i18next;
