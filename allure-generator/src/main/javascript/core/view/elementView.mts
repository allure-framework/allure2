import type { MountableElement, MountableLifecycle } from "./types.mts";

export const defineMountableElement = <
  TElement extends Element,
  TProperties extends object = Record<string, never>,
>(
  element: TElement,
  properties: TProperties & MountableLifecycle,
) => Object.assign(element, properties) as TElement & TProperties & MountableElement;
