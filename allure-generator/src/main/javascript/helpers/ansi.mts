import AnsiToHtml from "ansi-to-html";

const ansiConverter = new AnsiToHtml({
  fg: "black",
  bg: "black",
  newline: true,
  escapeXML: true,
});

const ansi = (input: unknown): string => ansiConverter.toHtml(input == null ? "" : String(input));

export default ansi;
