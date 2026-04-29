import i18next, { type Resource } from "i18next";
import am from "../../translations/am.json" with { type: "json" };
import az from "../../translations/az.json" with { type: "json" };
import br from "../../translations/br.json" with { type: "json" };
import de from "../../translations/de.json" with { type: "json" };
import en from "../../translations/en.json" with { type: "json" };
import es from "../../translations/es.json" with { type: "json" };
import fr from "../../translations/fr.json" with { type: "json" };
import he from "../../translations/he.json" with { type: "json" };
import hu from "../../translations/hu.json" with { type: "json" };
import isv from "../../translations/isv.json" with { type: "json" };
import it from "../../translations/it.json" with { type: "json" };
import ja from "../../translations/ja.json" with { type: "json" };
import ka from "../../translations/ka.json" with { type: "json" };
import kr from "../../translations/kr.json" with { type: "json" };
import nl from "../../translations/nl.json" with { type: "json" };
import pl from "../../translations/pl.json" with { type: "json" };
import ru from "../../translations/ru.json" with { type: "json" };
import sv from "../../translations/sv.json" with { type: "json" };
import tr from "../../translations/tr.json" with { type: "json" };
import zh from "../../translations/zh.json" with { type: "json" };
import gtag from "../../utils/gtag.mts";
import settings from "../services/settings.mts";
import type { TranslationBundle } from "./types.mts";

const translations = {
  am,
  az,
  br,
  de,
  en,
  es,
  fr,
  he,
  hu,
  isv,
  it,
  ja,
  ka,
  kr,
  nl,
  pl,
  ru,
  sv,
  tr,
  zh,
} as const satisfies Record<string, TranslationBundle>;

const DEFAULT_NAMESPACE = "translation";
const INTERNAL_LANGUAGE_ALIASES = {
  isv: "be",
} as const;

export type LanguageId = keyof typeof translations;
type I18NextLanguageId =
  | LanguageId
  | (typeof INTERNAL_LANGUAGE_ALIASES)[keyof typeof INTERNAL_LANGUAGE_ALIASES];
type TranslationResources = Record<I18NextLanguageId, { [DEFAULT_NAMESPACE]: TranslationBundle }>;

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
] as const satisfies ReadonlyArray<{ id: LanguageId; title: string; abbr?: string }>;

const resources = Object.fromEntries(
  LANGUAGES.map(({ id }) => [id, { [DEFAULT_NAMESPACE]: translations[id] }]),
) as TranslationResources satisfies Resource;

resources.be = resources.isv;

export function toI18nextLanguage(language: LanguageId) {
  if (language === "isv") {
    return INTERNAL_LANGUAGE_ALIASES.isv;
  }

  return language;
}

export async function initTranslations() {
  const language = settings.getLanguage() || "en";

  await i18next.init({
    lng: toI18nextLanguage(language),
    ns: [DEFAULT_NAMESPACE],
    defaultNS: DEFAULT_NAMESPACE,
    resources,
    interpolation: {
      escapeValue: false,
    },
    fallbackLng: "en",
    returnNull: false,
  });

  gtag("init_language", { language: language || "en" });
}

export function addTranslation(lang: LanguageId, json: TranslationBundle) {
  resources[lang] = { [DEFAULT_NAMESPACE]: json };

  if (!i18next.isInitialized) {
    return;
  }

  i18next.addResourceBundle(lang, DEFAULT_NAMESPACE, json, true, true);
}

export default i18next;
