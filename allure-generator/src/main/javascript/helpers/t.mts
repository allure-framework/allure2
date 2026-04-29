import i18next from "../core/i18n/index.mts";
import type { TranslationKey } from "../core/i18n/types.mts";

export type TranslationOptions = {
  hash?: Record<string, unknown>;
} | null;

export default function translate(
  key: TranslationKey | string,
  options: TranslationOptions = null,
) {
  const translationKey = key as TranslationKey;

  if (options?.hash) {
    return i18next.t(translationKey, options.hash);
  }

  return i18next.t(translationKey);
}
