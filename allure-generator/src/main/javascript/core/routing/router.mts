import { omit } from "../../shared/utils/collections.mts";

type RouteParameters = (string | null)[];
type QueryValue = string | number | boolean | null | undefined;

export type NavigateOptions = {
  replace?: boolean;
  trigger?: boolean;
};

export type QueryParams = Record<string, QueryValue>;

type RouteDefinition = {
  name: string;
  route: RegExp;
  callback: (fragment: string) => void;
};

type RouteHandler = (...args: RouteParameters) => void;

const toFragment = (fragment: string | null | undefined = "") =>
  `${fragment}`.replace(/^#/, "").replace(/^\//, "");

const optionalParam = /\((.*?)\)/g;
const namedParam = /(\(\?)?:\w+/g;
const splatParam = /\*\w+/g;
const escapeRegExp = /[-{}[\]+?.,\\^$|#\s]/g;

const routeToRegExp = (route: string) =>
  new RegExp(
    `^${route
      .replace(escapeRegExp, "\\$&")
      .replace(optionalParam, "(?:$1)?")
      .replace(namedParam, (match: string, optional: string | undefined) =>
        optional ? match : "([^/?]+)",
      )
      .replace(splatParam, "([^?]*?)")}(?:\\?([\\s\\S]*))?$`,
  );

const extractParameters = (route: RegExp, fragment: string): RouteParameters => {
  const values = route.exec(fragment)?.slice(1) || [];
  return values.map((value, index) => {
    if (index === values.length - 1) {
      return value || null;
    }

    return value ? decodeURIComponent(value) : null;
  });
};

class AppRouter {
  routes: RouteDefinition[];

  started: boolean;

  currentUrl: string | null;

  previousUrl: string | null;

  constructor() {
    this.routes = [];
    this.started = false;
    this.currentUrl = null;
    this.previousUrl = null;
    this.checkUrl = this.checkUrl.bind(this);
  }

  start({ silent = false } = {}) {
    if (this.started) {
      return;
    }

    this.started = true;
    window.addEventListener("hashchange", this.checkUrl);
    if (!silent) {
      this.loadUrl();
    }
  }

  stop() {
    if (!this.started) {
      return;
    }

    window.removeEventListener("hashchange", this.checkUrl);
    this.started = false;
  }

  checkUrl() {
    this.loadUrl();
  }

  loadUrl(fragment = this.getCurrentUrl()) {
    const normalizedFragment = toFragment(fragment);
    for (const { route, callback } of this.routes) {
      if (route.test(normalizedFragment)) {
        this.previousUrl = this.currentUrl;
        this.currentUrl = normalizedFragment;
        callback(normalizedFragment);
        return true;
      }
    }

    return false;
  }

  route(route: RegExp | string, name: string, callback: RouteHandler) {
    const routeRegExp = route instanceof RegExp ? route : routeToRegExp(route);

    this.routes.push({
      name,
      route: routeRegExp,
      callback: (fragment) => {
        const parameters = extractParameters(routeRegExp, fragment);
        callback(...parameters);
      },
    });

    return this;
  }

  navigate(fragment: string, options: NavigateOptions = {}) {
    const normalizedFragment = toFragment(fragment);
    const url = `${window.location.pathname}${window.location.search}#${normalizedFragment}`;

    window.history[options.replace ? "replaceState" : "pushState"]({}, document.title, url);

    if (options.trigger) {
      this.loadUrl(normalizedFragment);
    }

    return this;
  }

  getCurrentUrl() {
    return toFragment(window.location.hash);
  }

  reload() {
    this.loadUrl(this.getCurrentUrl());
  }

  to(pathname: string, query: QueryParams = {}, options: NavigateOptions = {}) {
    const normalizedQuery = omit(query, (value) => value === null);
    const params = new URLSearchParams();
    Object.entries(normalizedQuery || {}).forEach(([key, value]) => {
      if (typeof value !== "undefined") {
        params.set(key, String(value));
      }
    });
    const url = params.size > 0 ? `${pathname}?${params.toString()}` : pathname;
    return this.toUrl(url, options);
  }

  toUrl(url: string, options: NavigateOptions = {}) {
    return this.navigate(url, Object.assign({ trigger: true }, options));
  }

  setSearch(search: QueryParams) {
    const [pathname = ""] = this.getCurrentUrl().split("?");
    return this.to(pathname, search);
  }

  getUrlParams() {
    const [, search = ""] = this.getCurrentUrl().split("?");
    return Object.fromEntries(new URLSearchParams(search));
  }
}

export default new AppRouter();
