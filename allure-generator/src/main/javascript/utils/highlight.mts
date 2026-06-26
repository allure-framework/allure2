import highlight from "highlight.js/lib/core";
import bash from "highlight.js/lib/languages/bash";
import diff from "highlight.js/lib/languages/diff";
import json from "highlight.js/lib/languages/json";
import md from "highlight.js/lib/languages/markdown";
import xml from "highlight.js/lib/languages/xml";
import { isSyntaxHighlightOversized } from "../features/attachments/model/previewLimits.mts";

highlight.registerLanguage("xml", xml);
highlight.registerLanguage("bash", bash);
highlight.registerLanguage("markdown", md);
highlight.registerLanguage("diff", diff);
highlight.registerLanguage("json", json);

export const highlightElement = (element: HTMLElement) => {
  const content = element.textContent || "";
  if (isSyntaxHighlightOversized(content)) {
    element.dataset.syntaxHighlightSkipped = "true";
    return false;
  }

  highlight.highlightElement(element);
  return true;
};
