export const makeArray = <TValue,>(value: TValue | TValue[] | null | undefined): TValue[] =>
  Array.isArray(value) ? value : value ? [value] : [];
