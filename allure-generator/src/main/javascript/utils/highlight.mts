import highlight from "highlight.js/lib/core";
import bash from "highlight.js/lib/languages/bash";
import diff from "highlight.js/lib/languages/diff";
import json from "highlight.js/lib/languages/json";
import md from "highlight.js/lib/languages/markdown";
import xml from "highlight.js/lib/languages/xml";

highlight.registerLanguage("xml", xml);
highlight.registerLanguage("bash", bash);
highlight.registerLanguage("markdown", md);
highlight.registerLanguage("diff", diff);
highlight.registerLanguage("json", json);

export default highlight;
