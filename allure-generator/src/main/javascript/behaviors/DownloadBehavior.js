import { Behavior } from "backbone.marionette";
import TooltipView from "../components/tooltip/TooltipView";
import { reportDataUrl } from "../data/loader";
import { on } from "../decorators";

export default class DownloadBehavior extends Behavior {
  initialize() {
    this.tooltip = new TooltipView({
      position: "left",
    });
  }

  @on("click [data-download]")
  onDownloadableClick(e) {
    e.preventDefault();
    e.stopPropagation();
    const el = this.$(e.currentTarget);
    const path = el.data("download");
    if (!path) {
      return;
    }

    const contentType = el.data("download-type") || "application/octet-stream";
    const target = el.data("download-target") === "_blank";

    reportDataUrl(`${path}`, contentType)
      .then((href) => {
        const link = document.createElement("a");
        link.setAttribute("href", href);
        link.setAttribute("download", path);
        if (target) {
          link.setAttribute("target", "_blank");
        }
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      })
      .catch((error) => {
        this.tooltip.show(`Download error: ${error}`, el);
      });
  }
}
