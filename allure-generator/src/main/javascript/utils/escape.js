import { escapeExpression } from "handlebars/runtime";

export default function escape(source, ...tokens) {
  return source.reduce((result, s, i) => result + escapeExpression(tokens[i - 1]) + s);
}
