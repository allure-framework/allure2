import AnsiToHtml from "ansi-to-html";

const semanticAnsiColor = {
  primary: 256,
  muted: 257,
  danger: 258,
  success: 259,
  warning: 260,
  info: 261,
  unknown: 262,
  decorative: 263,
} as const;

const ansiColors: Record<number, string> = {
  0: "var(--color-text-primary)",
  1: "var(--color-intent-danger-text)",
  2: "var(--color-intent-success-text)",
  3: "var(--color-intent-warning-text)",
  4: "var(--color-intent-info-text)",
  5: "var(--color-status-unknown-text)",
  6: "var(--color-decorative-5-text)",
  7: "var(--color-text-primary)",
  8: "var(--color-text-muted)",
  9: "var(--color-intent-danger-text)",
  10: "var(--color-intent-success-text)",
  11: "var(--color-intent-warning-text)",
  12: "var(--color-intent-info-text)",
  13: "var(--color-status-unknown-text)",
  14: "var(--color-decorative-5-text)",
  15: "var(--color-text-primary)",
  [semanticAnsiColor.primary]: "var(--color-text-primary)",
  [semanticAnsiColor.muted]: "var(--color-text-muted)",
  [semanticAnsiColor.danger]: "var(--color-intent-danger-text)",
  [semanticAnsiColor.success]: "var(--color-intent-success-text)",
  [semanticAnsiColor.warning]: "var(--color-intent-warning-text)",
  [semanticAnsiColor.info]: "var(--color-intent-info-text)",
  [semanticAnsiColor.unknown]: "var(--color-status-unknown-text)",
  [semanticAnsiColor.decorative]: "var(--color-decorative-5-text)",
};

const xtermColorLevels = [0, 95, 135, 175, 215, 255];
const ansiEscape = "\u001B";
const ansiControlSequencePattern = new RegExp(`${ansiEscape}\\[([0-9;]*)m`, "g");

const getReadableAnsiColor = (red: number, green: number, blue: number) => {
  const brightness = red * 0.299 + green * 0.587 + blue * 0.114;
  const max = Math.max(red, green, blue);
  const min = Math.min(red, green, blue);

  if (red >= 150 && green >= 120 && blue <= 210 && red + green >= blue * 2.2) {
    return semanticAnsiColor.warning;
  }

  if (green >= red + 20 && green >= blue + 20) {
    return semanticAnsiColor.success;
  }

  if (red >= green + 30 && red >= blue + 30) {
    return semanticAnsiColor.danger;
  }

  if (blue >= red + 20 && blue >= green - 10) {
    return semanticAnsiColor.info;
  }

  if (red >= 120 && blue >= 120 && green <= max - 20) {
    return semanticAnsiColor.unknown;
  }

  if (max - min <= 32) {
    return brightness >= 150 || brightness <= 80
      ? semanticAnsiColor.primary
      : semanticAnsiColor.muted;
  }

  return semanticAnsiColor.primary;
};

const getXtermColorRgb = (colorIndex: number) => {
  if (colorIndex >= 16 && colorIndex <= 231) {
    const cubeIndex = colorIndex - 16;

    return {
      red: xtermColorLevels[Math.floor(cubeIndex / 36)],
      green: xtermColorLevels[Math.floor((cubeIndex % 36) / 6)],
      blue: xtermColorLevels[cubeIndex % 6],
    };
  }

  if (colorIndex >= 232 && colorIndex <= 255) {
    const level = (colorIndex - 232) * 10 + 8;

    return { red: level, green: level, blue: level };
  }
};

const isValidColorPart = (value: number) => Number.isInteger(value) && value >= 0 && value <= 255;

const normalizeEscapedAnsiSequences = (text: string) =>
  text.replace(/\\u001b(?=\[)/giu, ansiEscape).replace(/\\x1b(?=\[)/giu, ansiEscape);

const normalizeAnsiForegroundColors = (text: string) =>
  text.replace(ansiControlSequencePattern, (match, sequence: string) => {
    const codes = sequence.length === 0 ? [0] : sequence.split(";").map(Number);

    if (codes.some((code) => !Number.isInteger(code))) {
      return match;
    }

    const fragments: string[] = [];
    let displayCodes: number[] = [];
    const flushDisplayCodes = () => {
      if (displayCodes.length > 0) {
        fragments.push(`${ansiEscape}[${displayCodes.join(";")}m`);
        displayCodes = [];
      }
    };

    for (let index = 0; index < codes.length; ) {
      const code = codes[index];

      if (code === 38 && codes[index + 1] === 5 && isValidColorPart(codes[index + 2])) {
        const rgb = getXtermColorRgb(codes[index + 2]);

        flushDisplayCodes();
        fragments.push(
          `${ansiEscape}[38;5;${rgb ? getReadableAnsiColor(rgb.red, rgb.green, rgb.blue) : semanticAnsiColor.primary}m`,
        );
        index += 3;
        continue;
      }

      if (
        code === 38 &&
        codes[index + 1] === 2 &&
        isValidColorPart(codes[index + 2]) &&
        isValidColorPart(codes[index + 3]) &&
        isValidColorPart(codes[index + 4])
      ) {
        flushDisplayCodes();
        fragments.push(
          `${ansiEscape}[38;5;${getReadableAnsiColor(codes[index + 2], codes[index + 3], codes[index + 4])}m`,
        );
        index += 5;
        continue;
      }

      if (code === 48 && codes[index + 1] === 5 && isValidColorPart(codes[index + 2])) {
        flushDisplayCodes();
        fragments.push(`${ansiEscape}[48;5;${codes[index + 2]}m`);
        index += 3;
        continue;
      }

      if (
        code === 48 &&
        codes[index + 1] === 2 &&
        isValidColorPart(codes[index + 2]) &&
        isValidColorPart(codes[index + 3]) &&
        isValidColorPart(codes[index + 4])
      ) {
        flushDisplayCodes();
        fragments.push(
          `${ansiEscape}[48;2;${codes[index + 2]};${codes[index + 3]};${codes[index + 4]}m`,
        );
        index += 5;
        continue;
      }

      displayCodes.push(code);
      index += 1;
    }

    flushDisplayCodes();

    return fragments.join("");
  });

const ansiConverter = new AnsiToHtml({
  fg: "var(--color-text-primary)",
  bg: "none",
  newline: true,
  escapeXML: true,
  colors: ansiColors,
});

const ansi = (input: unknown): string =>
  ansiConverter.toHtml(
    normalizeAnsiForegroundColors(
      normalizeEscapedAnsiSequences(input == null ? "" : String(input)),
    ),
  );

export default ansi;
