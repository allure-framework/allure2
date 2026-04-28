export default function isDef(variable: unknown): boolean {
  return Boolean(variable) || typeof variable === "number";
}
