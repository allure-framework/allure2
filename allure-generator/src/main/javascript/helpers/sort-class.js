export default function(sortField, { field, order }) {
  if (field === sortField) {
    return order === "asc" ? "up" : "down";
  }
  return false;
}
