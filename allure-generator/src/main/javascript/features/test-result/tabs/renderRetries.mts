import dateHelper from "../../../helpers/date.mts";
import translate from "../../../helpers/t.mts";
import timeHelper from "../../../helpers/time.mts";
import b from "../../../shared/bem/index.mts";
import { createElement } from "../../../shared/dom.mts";

type RetryItem = {
  uid: string;
  status: string;
  time?: import("../../../types/report.mts").Time;
  statusDetails?: string;
};

export const renderRetries = ({ retries }: { retries: RetryItem[] | null }) =>
  createElement("div", {
    className: b("pane", "section"),
    children: retries?.length
      ? retries.map(({ uid, status, time, statusDetails }) => {
          const retryTime = translate("testResult.retries.time", {
            hash: {
              time: dateHelper(time?.start),
              date: timeHelper(time?.start),
            },
          });
          return createElement("a", {
            attrs: {
              "data-uid": uid,
              href: `#/testresult/${uid}`,
            },
            className: "link retry-row",
            children: [
              createElement("span", {
                className: `label label_status_${status}`,
                text: status,
              }),
              createElement("span", { text: retryTime }),
              createElement("pre", {
                className: "preformated-text",
                children: createElement("code", { text: statusDetails || "" }),
              }),
            ],
          });
        })
      : translate("testResult.retries.empty"),
  });
