declare module "*.css" {
  const content: string;
  export default content;
}

declare module "*.scss" {
  const content: string;
  export default content;
}

declare module "*.ico" {
  const content: string;
  export default content;
}

declare module "highlight.js/lib/core" {
  import highlight from "highlight.js";
  export default highlight;
}

declare module "highlight.js/lib/languages/*" {
  import type { LanguageFn } from "highlight.js";

  const language: LanguageFn;
  export default language;
}

interface Window {
  dataLayer?: unknown[];
  reportData?: Record<string, string>;
  reportDataReady?: boolean;
}
