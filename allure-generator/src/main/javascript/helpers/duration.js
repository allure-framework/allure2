import { isFunction } from "underscore";
import pad from "underscore.string/pad";

const dateTokens = [
  {
    suffix: "d",
    method: (time) => Math.floor(time.valueOf() / (24 * 3600 * 1000)),
  },
  {
    suffix: "h",
    method: "getUTCHours",
  },
  {
    suffix: "m",
    method: "getUTCMinutes",
  },
  {
    suffix: "s",
    method: "getUTCSeconds",
  },
  {
    pad: 3,
    suffix: "ms",
    method: "getUTCMilliseconds",
  },
];

export default function(timeInt, count) {
  if (timeInt === 0) {
    return "0s";
  }
  if (!timeInt) {
    return "Unknown";
  }
  const time = new Date(timeInt);
  const res = dateTokens
    .map(({ method, suffix, pad: pd }) => ({
      value: isFunction(method) ? method(time) : time[method](),
      suffix,
      pad: pd,
    }))
    .reduce(
      ({ hasValue, out }, token) => {
        hasValue = hasValue || token.value > 0;
        if (token.value > 0 || (hasValue && token.suffix !== "ms")) {
          out.push(token);
        }
        return { hasValue, out };
      },
      { hasValue: false, out: [] },
    )
    .out.map(function(token, index) {
      const value = index === 0 ? token.value : pad(token.value, token.pad || 2, "0");
      return value + token.suffix;
    });
  if (typeof count !== "number") {
    count = 2;
  }
  return res.slice(0, count).join(" ");
}
