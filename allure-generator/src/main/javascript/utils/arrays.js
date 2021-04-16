export function makeArray(value) {
  return Array.isArray(value) ? value : value ? [value] : [];
}
