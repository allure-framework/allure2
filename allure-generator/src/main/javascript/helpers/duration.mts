import { isFunction } from "../shared/utils/collections.mts";
import pad from "../shared/utils/pad.mts";

type DateTokenMethod =
  | ((time: Date) => number)
  | "getUTCHours"
  | "getUTCMinutes"
  | "getUTCSeconds"
  | "getUTCMilliseconds";

type DateToken = {
  suffix: string;
  method: DateTokenMethod;
  pad?: number;
};

type DurationToken = {
  value: number;
  suffix: string;
  pad?: number;
};

const dateTokens: DateToken[] = [
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

export default function formatDuration(timeInt: number | null | undefined, count = 2) {
  if (timeInt === 0) {
    return "0s";
  }
  if (!timeInt) {
    return "Unknown";
  }
  const time = new Date(timeInt);
  const res = dateTokens
    .map(({ method, suffix, pad: pd }) => ({
      value: isFunction<[Date], number>(method) ? method(time) : time[method](),
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
      { hasValue: false, out: [] as DurationToken[] },
    )
    .out.map(function (token, index) {
      const value = index === 0 ? token.value : pad(token.value, token.pad || 2, "0");
      return value + token.suffix;
    });
  return res.slice(0, count).join(" ");
}
