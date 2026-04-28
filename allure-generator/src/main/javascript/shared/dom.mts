export type DomChild = Node | string | number | false | null | undefined | readonly DomChild[];

type DomAttributes = Record<string, string | number | boolean | null | undefined>;

type CreateElementOptions = {
  attrs?: DomAttributes;
  children?: DomChild;
  className?: string;
  text?: string | number | null | undefined;
};

const isNode = (value: unknown): value is Node =>
  typeof Node !== "undefined" && value instanceof Node;

const appendChild = (parent: Node, child: DomChild): void => {
  if (child === null || typeof child === "undefined" || child === false) {
    return;
  }

  if (Array.isArray(child)) {
    child.forEach((item) => appendChild(parent, item));
    return;
  }

  if (isNode(child)) {
    parent.appendChild(child);
    return;
  }

  parent.appendChild(document.createTextNode(String(child)));
};

export const appendChildren = <TParent extends Node>(parent: TParent, ...children: DomChild[]) => {
  children.forEach((child) => appendChild(parent, child));
  return parent;
};

export const createFragment = (...children: DomChild[]): DocumentFragment =>
  appendChildren(document.createDocumentFragment(), ...children);

const SVG_NS = "http://www.w3.org/2000/svg";

export const createFragmentFromHtml = (html: string, context?: Node) => {
  const range = document.createRange();
  const root =
    context || document.body || document.documentElement || document.createElement("div");

  if (root.nodeType === Node.DOCUMENT_NODE) {
    const documentRoot = root as Document;
    range.selectNodeContents(documentRoot.body || documentRoot.documentElement);
  } else {
    range.selectNodeContents(root);
  }

  return range.createContextualFragment(html);
};

export const createElement = <K extends keyof HTMLElementTagNameMap>(
  tagName: K,
  { attrs = {}, children, className, text }: CreateElementOptions = {},
) => {
  const element = document.createElement(tagName);

  if (className) {
    element.className = className;
  }

  if (typeof text !== "undefined" && text !== null) {
    element.textContent = String(text);
  }

  Object.entries(attrs).forEach(([name, value]) => {
    if (value === null || typeof value === "undefined" || value === false) {
      return;
    }

    element.setAttribute(name, value === true ? "" : String(value));
  });

  if (typeof children !== "undefined") {
    appendChild(element, children);
  }

  return element;
};

export const createSvgElement = (
  tagName: string,
  { attrs = {}, children, className }: Omit<CreateElementOptions, "text"> = {},
) => {
  const element = document.createElementNS(SVG_NS, tagName);

  if (className) {
    element.setAttribute("class", className);
  }

  Object.entries(attrs).forEach(([name, value]) => {
    if (value === null || typeof value === "undefined" || value === false) {
      return;
    }

    element.setAttribute(name, value === true ? "" : String(value));
  });

  if (typeof children !== "undefined") {
    appendChild(element, children);
  }

  return element;
};
