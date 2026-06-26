import { AttachmentPreviewView } from "../model/attachmentPreviewView.mts";
import {
  getAttachmentPreviewOversizeReason,
  isAttachmentPreviewOversized,
} from "../model/previewLimits.mts";
import { CodeAttachmentPreviewView } from "./CodeAttachmentPreviewView.mts";
import { FallbackAttachmentPreviewView } from "./FallbackAttachmentPreviewView.mts";
import { getHtmlPreviewOversizeReason } from "./HtmlAttachmentPreviewView.helpers.mts";
import { HtmlAttachmentPreviewView } from "./HtmlAttachmentPreviewView.mts";
import { HttpAttachmentView } from "./HttpAttachmentView.mts";
import { ImageAttachmentPreviewView } from "./ImageAttachmentPreviewView.mts";
import { PlaywrightTraceAttachmentView } from "./PlaywrightTraceAttachmentView.mts";
import { ScreenDiffAttachmentView } from "./ScreenDiffView.mts";
import { SvgAttachmentPreviewView } from "./SvgAttachmentPreviewView.mts";
import { TableAttachmentPreviewView } from "./TableAttachmentPreviewView.mts";
import { TextAttachmentPreviewView } from "./TextAttachmentPreviewView.mts";
import { UriAttachmentPreviewView } from "./UriAttachmentPreviewView.mts";
import { VideoAttachmentPreviewView } from "./VideoAttachmentPreviewView.mts";

import type { AttachmentPreviewComponent } from "./BaseAttachmentPreviewView.mts";

const attachmentPreviewViews: Record<AttachmentPreviewView, AttachmentPreviewComponent> = {
  [AttachmentPreviewView.Archive]: FallbackAttachmentPreviewView,
  [AttachmentPreviewView.Code]: CodeAttachmentPreviewView,
  [AttachmentPreviewView.Html]: HtmlAttachmentPreviewView,
  [AttachmentPreviewView.HttpExchange]: HttpAttachmentView,
  [AttachmentPreviewView.Image]: ImageAttachmentPreviewView,
  [AttachmentPreviewView.PlaywrightTrace]: PlaywrightTraceAttachmentView,
  [AttachmentPreviewView.ScreenDiff]: ScreenDiffAttachmentView,
  [AttachmentPreviewView.Svg]: SvgAttachmentPreviewView,
  [AttachmentPreviewView.Table]: TableAttachmentPreviewView,
  [AttachmentPreviewView.Text]: TextAttachmentPreviewView,
  [AttachmentPreviewView.Uri]: UriAttachmentPreviewView,
  [AttachmentPreviewView.Video]: VideoAttachmentPreviewView,
};

const sizeLimitedPreviewViews = new Set<AttachmentPreviewView>([
  AttachmentPreviewView.Code,
  AttachmentPreviewView.Html,
  // HTTP exchange is structured, but the current preview still fetches and parses the whole JSON payload.
  AttachmentPreviewView.HttpExchange,
  AttachmentPreviewView.ScreenDiff,
  AttachmentPreviewView.Svg,
  AttachmentPreviewView.Table,
  AttachmentPreviewView.Text,
  AttachmentPreviewView.Uri,
]);

const getPreviewDisabledReason = (options: Parameters<AttachmentPreviewComponent>[0]) => {
  if (
    !options.view ||
    !sizeLimitedPreviewViews.has(options.view) ||
    !isAttachmentPreviewOversized(options.attachment.size)
  ) {
    return null;
  }

  return options.view === AttachmentPreviewView.Html
    ? getHtmlPreviewOversizeReason()
    : getAttachmentPreviewOversizeReason();
};

export const PreviewView: AttachmentPreviewComponent = (options) => {
  const previewDisabledReason = getPreviewDisabledReason(options);
  if (previewDisabledReason) {
    return FallbackAttachmentPreviewView({
      ...options,
      previewDisabledReason,
    });
  }

  const createPreviewView = options.view ? attachmentPreviewViews[options.view] : null;

  return (createPreviewView ?? FallbackAttachmentPreviewView)(options);
};
