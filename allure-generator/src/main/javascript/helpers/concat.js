export default function concat(...args) {
  return args.slice(0, -1).join("");
}
