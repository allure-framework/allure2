import b from "../../../shared/bem/index.mts";
import { createElement, createFragment } from "../../../shared/dom.mts";
import { createStatusDetailsElement } from "./renderStatusDetails.mts";

type OverviewRenderOptions = {
  cls: string;
  status: string;
  statusMessage?: string;
  statusTrace?: string;
};

export const createTestResultOverviewContent = ({ cls, status, ...data }: OverviewRenderOptions) =>
  createFragment(
    data.statusMessage || data.statusTrace
      ? createElement("div", {
          className: b("alert", "wide", { status }),
          children: createStatusDetailsElement({
            status,
            ...data,
          }),
        })
      : null,
    createElement("div", {
      className: b(cls, "tags"),
    }),
    createElement("div", {
      className: b(cls, "before"),
    }),
    createElement("div", {
      className: b(cls, "execution"),
    }),
    createElement("div", {
      className: b(cls, "after"),
    }),
  );
