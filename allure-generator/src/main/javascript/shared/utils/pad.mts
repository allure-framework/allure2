export type PadDirection = "left" | "right";

const repeat = (value: string, size: number) => `${value}`.repeat(Math.max(size, 0));

export default function pad(
  value: string | number,
  length: number,
  padString = " ",
  type: PadDirection = "left",
) {
  const stringValue = `${value}`;
  const padLength = Math.max(length - stringValue.length, 0);
  if (padLength === 0) {
    return stringValue;
  }

  const left = type === "right" ? "" : repeat(padString, padLength).slice(0, padLength);
  const right = type === "left" ? "" : repeat(padString, padLength).slice(0, padLength);
  return `${left}${stringValue}${right}`;
}
