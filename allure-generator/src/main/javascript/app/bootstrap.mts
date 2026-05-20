import "normalize-css/normalize.css";
import "highlight.js/styles/github.css";
import "../styles.scss";
import i18next, { initTranslations } from "../core/i18n/index.mts";
import router from "../core/routing/router.mts";
import { attachMountable, destroyMountable } from "../core/view/mountables.mts";
import { initTheme } from "../shared/theme.mts";

type Mountable = import("../core/registry/types.mts").Mountable;
type RouteArguments = import("../core/registry/types.mts").RouteArguments;
type AppRouter = Pick<typeof router, "route">;
type RegisterTabRoutes = (options: {
  notFound: () => Mountable;
  router: AppRouter;
  showView: <TArgs extends RouteArguments>(
    factory: (...args: TArgs) => Mountable,
  ) => (...args: TArgs) => void;
}) => void;

type AppDependencies = {
  registerTabRoutes: RegisterTabRoutes;
  createNotFoundView: () => Mountable;
  createTestResultLayout: (uid: string, tabName?: string | null) => Mountable;
};

type RouteAwareMountable = Mountable & {
  onRouteUpdate?: (...args: RouteArguments) => void;
  shouldKeepState?: (...args: RouteArguments) => boolean;
};

// https://github.com/d3/d3-timer/pull/21
if (typeof window.requestAnimationFrame === "function") {
  window.requestAnimationFrame = window.requestAnimationFrame.bind(window);
}

const rootPath = (path: string | null = "") => path?.split("/")[0] || "";

const applyThemePlatformHints = () => {
  if (window.navigator.platform.startsWith("Mac")) {
    document.documentElement.dataset.os = "mac";
  }
};

const createMountController = () => {
  const controller: {
    element: Element | null;
    currentView: Mountable | null;
    getElement: () => Element | null;
    show: (view: Mountable) => Mountable;
    reset: () => void;
  } = {
    element: null,
    currentView: null,

    getElement() {
      if (!this.element) {
        this.element = document.querySelector("#content");
      }

      return this.element;
    },

    show(view: Mountable) {
      const element = this.getElement();
      if (!element) {
        throw new Error('Mount element "#content" not found');
      }

      this.reset();
      this.currentView = view;
      attachMountable(element, view);

      return view;
    },

    reset() {
      destroyMountable(this.currentView);
      this.currentView = null;

      const element = this.getElement();
      if (element) {
        element.replaceChildren();
      }
    },
  };

  return controller;
};

export const createApp = ({
  registerTabRoutes,
  createNotFoundView,
  createTestResultLayout,
}: AppDependencies) => {
  const mountController = createMountController();

  const noTabChange = () => rootPath(router.getCurrentUrl()) === rootPath(router.previousUrl);

  const showView = <TArgs extends RouteArguments>(factory: (...args: TArgs) => Mountable) => {
    return (...args: TArgs) => {
      const current = App.getView() as RouteAwareMountable | null;
      if (
        current &&
        current.onRouteUpdate &&
        current.shouldKeepState &&
        noTabChange() &&
        current.shouldKeepState(...args)
      ) {
        current.onRouteUpdate(...args);
      } else {
        App.showView(factory(...args));
      }
    };
  };

  const notFound = () => createNotFoundView();

  const App = {
    started: false,

    start() {
      if (this.started) {
        return;
      }

      this.started = true;
      initTheme();
      applyThemePlatformHints();

      initTranslations().then(() => {
        registerTabRoutes({ notFound, router, showView });
        router.route(
          "testresult/:uid(/)(:tabName)",
          "testresultPage",
          showView((uid: string | null, tabName: string | null) =>
            uid ? createTestResultLayout(uid, tabName || undefined) : notFound(),
          ),
        );
        router.route("*default", "notFound", showView(notFound));
        router.start();
        document.dir = i18next.dir();
        i18next.on("languageChanged", () => {
          mountController.reset();
          router.reload();
          document.dir = i18next.dir();
        });
      });
    },

    showView: (view: Mountable) => mountController.show(view),
    getView: () => mountController.currentView,
  };

  return { App, notFound, showView };
};
