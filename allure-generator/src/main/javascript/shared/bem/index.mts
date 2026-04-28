export type BemModifierValue = string | number | boolean | null | undefined;
export type BemModifiers = Record<string, BemModifierValue>;

type BemFormatterOptions = {
  tailSpace: string;
  elementSeparator: string;
  modSeparator: string;
  modValueSeparator: string;
  classSeparator: string;
  isFullModifier: boolean;
  isFullBoolValue: boolean;
};

export type BemFunction = {
  (block: string, element?: string | BemModifiers | null, modifiers?: BemModifiers): string;
  with: (...args: Parameters<BemFunction>) => BemFunction;
  lock: (...args: Parameters<BemFunction>) => BemFunction;
  B: typeof BemFormatter;
};

class BemFormatter {
  tailSpace: string;

  elementSeparator: string;

  modSeparator: string;

  modValueSeparator: string;

  classSeparator: string;

  isFullModifier: boolean;

  isFullBoolValue: boolean;

  constructor(options: Partial<BemFormatterOptions> = {}) {
    this.tailSpace = options.tailSpace || "";
    this.elementSeparator = options.elementSeparator || "__";
    this.modSeparator = options.modSeparator || "_";
    this.modValueSeparator = options.modValueSeparator || "_";
    this.classSeparator = options.classSeparator || " ";
    this.isFullModifier = options.isFullModifier ?? true;
    this.isFullBoolValue = options.isFullBoolValue ?? false;
  }

  stringifyModifier(base: string, modifierKey: string, modifierValue: BemModifierValue) {
    if (typeof modifierValue === "undefined") {
      return "";
    }

    if (!this.isFullBoolValue && modifierValue === false) {
      return "";
    }

    let result = `${this.classSeparator}${base}${this.modSeparator}${modifierKey}`;
    if (this.isFullBoolValue || modifierValue !== true) {
      result += `${this.modValueSeparator}${String(modifierValue)}`;
    }

    return result;
  }

  stringifyModifiers(base: string, modifiers: BemModifiers = {}) {
    const modifierBase = this.isFullModifier ? base : "";
    return Object.entries(modifiers)
      .map(([modifierKey, modifierValue]) =>
        this.stringifyModifier(modifierBase, modifierKey, modifierValue),
      )
      .join("");
  }

  stringify(block: string, element?: string | BemModifiers | null, modifiers?: BemModifiers) {
    let nextElement = element;
    let nextModifiers = modifiers;

    if (
      nextElement &&
      typeof nextElement === "object" &&
      !Array.isArray(nextElement) &&
      typeof nextModifiers === "undefined"
    ) {
      nextModifiers = nextElement;
      nextElement = null;
    }

    let className = String(block);
    if (nextElement) {
      className += `${this.elementSeparator}${String(nextElement)}`;
    }

    if (nextModifiers) {
      className += this.stringifyModifiers(className, nextModifiers);
    }

    return `${className}${this.tailSpace}`;
  }
}

const withMixin = function (this: BemFunction, ...args: Parameters<BemFunction>) {
  return this.bind(null, ...args) as unknown as BemFunction;
};

const createBemFormatter = (options: Partial<BemFormatterOptions> = {}): BemFunction => {
  const formatter = new BemFormatter(options);
  const b = formatter.stringify.bind(formatter) as unknown as BemFunction;
  b.with = withMixin;
  b.lock = withMixin;
  b.B = BemFormatter;
  return b;
};

export { BemFormatter as B };

export default createBemFormatter();
