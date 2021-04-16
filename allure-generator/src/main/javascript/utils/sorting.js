import { sortBy } from "underscore";

function getByPath(obj, path) {
  return path.split(".").reduce((context, key) => context[key], obj);
}

export function updateSort(sortField, { field, order }) {
  if (sortField === field) {
    order = order === "asc" ? "desc" : "asc";
    return { field, order };
  }
  return { field: sortField, order: "asc" };
}

export function doSort(array, { field, order }) {
  const result = sortBy(array, (item) => getByPath(item, field));
  if (order === "desc") {
    return result.reverse();
  }
  return result;
}
