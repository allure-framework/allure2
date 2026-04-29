import en from "../../translations/en.json" with { type: "json" };

type DeepStringify<T> = T extends string
  ? string
  : T extends Record<string, unknown>
    ? { [K in keyof T]: DeepStringify<T[K]> }
    : T;

type LeafTranslationKey<T, Prefix extends string = ""> =
  T extends Record<string, unknown>
    ? {
        [K in Extract<keyof T, string>]: T[K] extends string
          ? `${Prefix}${K}`
          : T[K] extends Record<string, unknown>
            ? LeafTranslationKey<T[K], `${Prefix}${K}.`>
            : never;
      }[Extract<keyof T, string>]
    : never;

export type TranslationBundle = DeepStringify<typeof en>;

export type TranslationKey = LeafTranslationKey<TranslationBundle>;
