import b from "b_";

export default function (...args) {
  const options = args.pop();
  return b(...args, options.hash);
}
