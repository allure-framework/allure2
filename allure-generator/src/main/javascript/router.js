import urlLib from "url";
import { history, Router } from "backbone";
import { omit } from "underscore";

class AppRouter extends Router {
  currentUrl = null;

  constructor() {
    super({
      routes: {
        "testresult/:uid(/)(:tabName)": "testresultPage",
        "*default": "notFound",
      },
    });
    this.on("route", this.onRouteChange, this);
  }

  onRouteChange() {
    this.previousUrl = this.currentUrl;
    this.currentUrl = this.getCurrentUrl();
  }

  getCurrentUrl() {
    return history.getFragment();
  }

  reload() {
    history.loadUrl(this.getCurrentUrl());
  }

  to(pathname, query, options) {
    query = omit(query, (value) => value === null);
    const url = urlLib.format({ pathname, query });
    return this.toUrl(url, options);
  }

  toUrl(url, options) {
    return this.navigate(url, Object.assign({ trigger: true }, options));
  }

  setSearch(search) {
    const { pathname } = urlLib.parse(this.getCurrentUrl());
    return this.to(pathname, search);
  }

  getUrlParams() {
    const parsed = urlLib.parse(this.getCurrentUrl(), true);
    return parsed ? parsed.query : {};
  }
}

export default new AppRouter();
