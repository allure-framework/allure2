type NestedArray<TItem> = ReadonlyArray<TItem | NestedArray<TItem>>;

export const isFunction = <TArgs extends unknown[] = unknown[], TResult = unknown>(
  value: unknown,
): value is (...args: TArgs) => TResult => typeof value === "function";

export const omit = <TObject extends Record<string, unknown>>(
  object: TObject | null | undefined,
  predicateOrKeys:
    | ((value: TObject[keyof TObject], key: Extract<keyof TObject, string>) => boolean)
    | Extract<keyof TObject, string>
    | ReadonlyArray<Extract<keyof TObject, string>>,
) => {
  const result: Partial<TObject> = {};
  const predicate =
    typeof predicateOrKeys === "function"
      ? predicateOrKeys
      : (_value: TObject[keyof TObject], key: Extract<keyof TObject, string>) =>
          (Array.isArray(predicateOrKeys) ? predicateOrKeys : [predicateOrKeys]).includes(key);

  Object.entries(object || {}).forEach(([key, value]) => {
    if (!predicate(value as TObject[keyof TObject], key as Extract<keyof TObject, string>)) {
      result[key as keyof TObject] = value as TObject[keyof TObject];
    }
  });

  return result;
};

export const uniq = <TItem,>(items: ReadonlyArray<TItem> | null | undefined) =>
  Array.from(new Set(items || []));

const flattenInto = <TItem,>(items: NestedArray<TItem>, result: TItem[]) => {
  items.forEach((item) => {
    if (Array.isArray(item)) {
      flattenInto(item, result);
    } else {
      result.push(item as TItem);
    }
  });
};

export const flatten = <TItem,>(items: NestedArray<TItem> | null | undefined): TItem[] => {
  const result: TItem[] = [];
  if (items) {
    flattenInto(items, result);
  }
  return result;
};

export const findWhere = <TItem extends Record<string, unknown>>(
  items: ReadonlyArray<TItem> | null | undefined,
  attrs: Partial<Record<Extract<keyof TItem, string>, unknown>>,
) =>
  (items || []).find((item) =>
    Object.entries(attrs).every(([key, value]) => item && item[key] === value),
  );

export const last = <TItem,>(items: ReadonlyArray<TItem> | null | undefined) =>
  items && items.length ? items[items.length - 1] : undefined;

export const range = (start: number, stop?: number, step = 1) => {
  let actualStart = start;
  let actualStop = stop;
  if (typeof actualStop === "undefined") {
    actualStop = actualStart;
    actualStart = 0;
  }

  const result = [];
  for (let index = actualStart; index < actualStop; index += step) {
    result.push(index);
  }
  return result;
};
