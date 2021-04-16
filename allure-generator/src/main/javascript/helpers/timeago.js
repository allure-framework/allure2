import distanceInWordsToNow from "date-fns/distance_in_words_to_now";
import format from "date-fns/format";
import { SafeString } from "handlebars/runtime";

const threshold = 24 * 3600 * 1000;

export default function(date) {
  const dateString =
    Date.now() - date < threshold ? distanceInWordsToNow(date) : format(date, "DD MMMM YYYY");
  return new SafeString(`<span title="${format("DD MMMM YYYY, H:mm:ss")}">${dateString}</span>`);
}
