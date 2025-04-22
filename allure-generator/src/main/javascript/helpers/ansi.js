import AnsiToHtml from "ansi-to-html";
import {SafeString} from "handlebars/runtime";

const ansiConverter = new AnsiToHtml({
    fg: "black",
    bg: "black",
    newline: true,
});

export default function (input) {
    return new SafeString(ansiConverter.toHtml(input));
};
