export default function(...values) {
  values.pop();
  for (const value of values) {
    if (value) {
      return value;
    }
  }
}
