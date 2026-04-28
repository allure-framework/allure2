import "i18next";

type TranslationBundle = import("../core/i18n/types.mts").TranslationBundle;

declare module "i18next" {
  interface CustomTypeOptions {
    defaultNS: "translation";
    resources: {
      translation: TranslationBundle;
    };
    returnNull: false;
  }
}
