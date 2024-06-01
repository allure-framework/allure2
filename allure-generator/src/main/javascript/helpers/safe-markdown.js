import DOMPurify from "dompurify";
import { marked } from "marked";

const DOMPurifyInstance = DOMPurify(window);

const renderer = new marked.Renderer();

renderer.strong = (text) => `<strong>${text}</strong>`;
renderer.em = (text) => `<em>${text}</em>`;
renderer.link = (href, title, text) => {
    const titleAttr = title ? ` title="${title}"` : "";
    return `<a href="${href}"${titleAttr} target="_blank">${text}</a>`;
};

// Disabling other formats
renderer.heading = () => "";
renderer.paragraph = (text) => text;
renderer.image = () => "";
renderer.blockquote = () => "";
renderer.list = () => "";
renderer.listitem = () => "";
renderer.code = () => "";
renderer.html = () => "";
renderer.hr = () => "";
renderer.table = () => "";
renderer.tablerow = () => "";
renderer.tablecell = () => "";

marked.setOptions({
    renderer,
    gfm: false,
    breaks: false,
    smartLists: false,
    smartypants: false,
});

export default function (text) {
    return DOMPurifyInstance.sanitize(marked(text));
}
