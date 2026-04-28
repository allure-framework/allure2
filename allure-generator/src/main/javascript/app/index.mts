import { registerTabRoutes } from "../core/registry/index.mts";
import { TestResultLayout } from "../features/test-result/runtime.mts";
import translate from "../helpers/t.mts";
import { createApp } from "./bootstrap.mts";
import ErrorLayout from "./ErrorLayout.mts";

const { App, notFound, showView } = createApp({
  registerTabRoutes,
  createNotFoundView: () => ErrorLayout({ code: 401, message: translate("errors.notFound") }),
  createTestResultLayout: (uid, tabName) =>
    TestResultLayout({ uid, tabName: tabName || undefined }),
});

export { App, notFound, showView };
