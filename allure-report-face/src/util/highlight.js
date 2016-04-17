import highlight from 'highlight.js/lib/highlight';
import xml from 'highlight.js/lib/languages/xml';
import bash from 'highlight.js/lib/languages/bash';
import md from 'highlight.js/lib/languages/markdown';
import diff from 'highlight.js/lib/languages/diff';
import json from 'highlight.js/lib/languages/json';

highlight.registerLanguage('xml', xml);
highlight.registerLanguage('bash', bash);
highlight.registerLanguage('markdown', md);
highlight.registerLanguage('diff', diff);
highlight.registerLanguage('json', json);

export default highlight;
