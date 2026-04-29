import "./RetriesView.scss";
import router from "../../../core/routing/router.mts";
import { bindDelegatedEvents } from "../../../core/view/domEvents.mts";
import { defineMountableElement } from "../../../core/view/elementView.mts";
import { renderRetries } from "./renderRetries.mts";

type TestResult = import("../../../types/report.mts").TestResult;

type RetriesOptions = {
  data: TestResult;
};

const createRetriesView = (options: RetriesOptions) => {
  const el = defineMountableElement(document.createElement("div"), {
    onRetryClick(event: Event) {
      const uid = (event.currentTarget as HTMLElement).dataset.uid;
      router.toUrl(`#testresult/${uid}`);
    },
  });
  let releaseEvents = () => {};
  Object.assign(el, {
    render() {
      releaseEvents();
      const extra = options?.data.extra;
      const retries = extra ? extra.retries : null;
      el.className = "test-result-retries";
      el.replaceChildren(
        renderRetries({
          retries: Array.isArray(retries)
            ? retries.map((retry) => ({
                uid: retry.uid,
                status: retry.status,
                time: retry.time,
                statusDetails:
                  typeof retry.statusDetails === "string" ? retry.statusDetails : undefined,
              }))
            : null,
        }),
      );
      releaseEvents = bindDelegatedEvents({
        root: el,
        events: {
          "click .retry-row": "onRetryClick",
        },
        context: el,
      });
      return el;
    },
    destroy() {
      releaseEvents();
      el.remove();
    },
  });

  return el;
};

export default createRetriesView;
