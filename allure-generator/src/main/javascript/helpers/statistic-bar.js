import { SafeString } from "handlebars/runtime";
import { values } from "../utils/statuses";

export default function(statistic) {
  const fill = values
    .map((status) => {
      const count = !statistic || typeof statistic[status] === "undefined" ? 0 : statistic[status];
      return count === 0 ? "" : `<span class="label label_status_${status}">${count}</span> `;
    })
    .join("");
  return new SafeString(`${fill}`);
}
