import { SafeString } from "handlebars/runtime";

export default function(status = "unknown") {
  return new SafeString(`<span class="fa fa-chevron-right fa-fw text_status_${status}"></span>`);
}
