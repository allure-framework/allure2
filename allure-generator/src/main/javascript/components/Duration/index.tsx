import "./styles.scss";
import * as React from "react";
import * as bem from "b_";
import { pad } from "underscore.string";

interface DateTokenConfig {
  suffix: string;
  method: (time: Date) => number;
  padding?: number;
}

interface DateTokenResult {
  suffix: string;
  value: number;
  padding?: number;
}

const dateTokens: Array<DateTokenConfig> = [
  {
    suffix: "d",
    method: time => Math.floor(time.valueOf() / (24 * 3600 * 1000)),
  },
  {
    suffix: "h",
    method: time => time.getUTCHours(),
  },
  {
    suffix: "m",
    method: time => time.getUTCMinutes(),
  },
  {
    suffix: "s",
    method: time => time.getUTCSeconds(),
  },
  {
    padding: 3,
    suffix: "ms",
    method: time => time.getUTCMilliseconds(),
  },
];

const b = bem.with("Duration");

const Duration: React.SFC<{ value?: number; count?: number }> = ({ value, count }) => {
  if (value === 0) {
    return <span className={b()}>0s</span>;
  }
  if (!value) {
    return <span className={b()}>Unknown</span>;
  }
  const time = new Date(value);
  const result = dateTokens
    .map(({ method, suffix, padding }) => ({
      value: method(time),
      suffix,
      padding,
    }))
    .reduce(
      ({ hasValue, out }, token: DateTokenResult) => {
        hasValue = hasValue || token.value > 0;
        if (token.value > 0 || (hasValue && token.suffix !== "ms")) {
          out.push(token);
        }
        return { hasValue, out };
      },
      { hasValue: false, out: [] as DateTokenResult[] },
    )
    .out.map((token: DateTokenResult, index) => {
      const formatted =
        index === 0 ? token.value : pad((token.value || 0).toString(), token.padding || 2, "0");
      return formatted + token.suffix;
    });

  return <span className={b()}>{result.slice(0, count || 2).join(" ")}</span>;
};

export default Duration;
