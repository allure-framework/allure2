export type MountableLifecycle = {
  render?: () => unknown;
  destroy?: () => void;
  attachToDom?: () => void;
  detachFromDom?: () => void;
  update?: (...args: unknown[]) => void;
};

export type MountableElement = Element & MountableLifecycle;

export type MountableAdapter = MountableLifecycle & {
  mountElement: Element;
};

export type Mountable = MountableElement | MountableAdapter;

export type MountableFactory<TOptions = void, TMountable extends Mountable = Mountable> = [
  TOptions,
] extends [void]
  ? (options?: undefined) => TMountable
  : (options: TOptions) => TMountable;

export type MountTargetReference = {
  el?: Element | null;
};

export type MountTarget =
  | Element
  | string
  | MountTargetReference
  | Element[]
  | NodeListOf<Element>
  | HTMLCollectionOf<Element>
  | null
  | undefined;
