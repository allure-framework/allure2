import "font-awesome/css/font-awesome.css";
import "./styles.scss";
import { history } from "backbone";
import { Application, Behaviors } from "backbone.marionette";
import * as behaviors from "./behaviors";
import translate from "./helpers/t";
import { ErrorLayout } from "./layouts/error/ErrorLayout";
import TestResultLayout from "./layouts/testresult/TestResultLayout";
import router from "./router";
import i18next, { initTranslations } from "./utils/translation";

// https://github.com/d3/d3-timer/pull/21
if (typeof window.requestAnimationFrame === "function") {
  window.requestAnimationFrame = window.requestAnimationFrame.bind(window);
}

Behaviors.behaviorsLookup = behaviors;

const rootPath = (path) => {
  return path.split("/")[0];
};

const noTabChange = () => {
  return rootPath(router.getCurrentUrl()) === rootPath(router.currentUrl);
};

export const showView = (factory) => {
  return (...args) => {
    const current = App.getView();
    if (current && noTabChange() && current.shouldKeepState(...args)) {
      current.onRouteUpdate(...args);
    } else {
      App.showView(factory(...args));
    }
  };
};

export const notFound = () => {
  return new ErrorLayout({ code: 404, message: translate("errors.notFound") });
};

const App = new Application({
  region: "#content",
});

App.on("start", () => {
  initTranslations().then(() => {
    history.start();
    document.dir = i18next.dir();
    i18next.on("languageChanged", () => {
      App.getRegion().reset();
      router.reload();
      document.dir = i18next.dir();
    });
  });

  router.on("route:notFound", showView(notFound));
  router.on(
    "route:testresultPage",
    showView((uid, tabName) => new TestResultLayout({ uid, tabName })),
  );
});

export { App };
