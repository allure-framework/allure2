declare module "b_" {
  interface BemBuilder {
    (): string;
    (block: string, element?: string, modifiers?: object): string;
    (element: string, modifiers?: object): string;

    with(block: string): BemBuilder;
  }

  const bem: BemBuilder;

  export = bem;
}
