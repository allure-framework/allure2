import StateStore from "./StateStore.mts";

type PersistentStateStoreOptions<TState extends Record<string, unknown>> = {
  storageKey: string;
  defaults?: Partial<TState>;
};

const memoryStorage = new Map<string, string>();
let useMemoryStorage = false;

const getLocalStorage = () => {
  if (useMemoryStorage || typeof window === "undefined") {
    return null;
  }

  try {
    return window.localStorage;
  } catch {
    useMemoryStorage = true;
    return null;
  }
};

const readStoredValue = (storageKey: string) => {
  const storage = getLocalStorage();
  if (!storage) {
    return memoryStorage.get(storageKey) ?? null;
  }

  try {
    const value = storage.getItem(storageKey);
    if (value !== null) {
      memoryStorage.set(storageKey, value);
    }
    return value;
  } catch {
    useMemoryStorage = true;
    return memoryStorage.get(storageKey) ?? null;
  }
};

const writeStoredValue = (storageKey: string, value: string) => {
  memoryStorage.set(storageKey, value);

  const storage = getLocalStorage();
  if (!storage) {
    return;
  }

  try {
    storage.setItem(storageKey, value);
  } catch {
    useMemoryStorage = true;
  }
};

const readStoredState = <TState extends Record<string, unknown>>(storageKey: string) => {
  const rawValue = readStoredValue(storageKey);
  if (!rawValue) {
    return {};
  }

  try {
    return JSON.parse(rawValue) as Partial<TState>;
  } catch {
    return {};
  }
};

export default class PersistentStateStore<
  TState extends Record<string, unknown> = Record<string, unknown>,
> extends StateStore<TState> {
  storageKey: string;

  constructor({ storageKey, defaults = {} }: PersistentStateStoreOptions<TState>) {
    super({
      ...defaults,
      ...readStoredState<TState>(storageKey),
    });
    this.storageKey = storageKey;
  }

  persist() {
    writeStoredValue(this.storageKey, JSON.stringify(this.toJSON()));
    return this;
  }

  set(
    key: Partial<TState> | string,
    value: unknown | import("./StateStore.mts").StateChangeOptions = undefined,
    options: import("./StateStore.mts").StateChangeOptions = {},
  ) {
    if (typeof key === "string") {
      super.set(key, value as unknown, options);
    } else {
      super.set(key, (value as import("./StateStore.mts").StateChangeOptions) || {});
    }
    return this.persist();
  }

  save(
    ...args:
      | [Partial<TState>, import("./StateStore.mts").StateChangeOptions?]
      | [string, unknown, import("./StateStore.mts").StateChangeOptions?]
  ) {
    if (typeof args[0] === "string") {
      const [key, value, options] = args;
      super.set(key, value, options);
    } else {
      const [updates, options] = args;
      super.set(updates, options || {});
    }
    return this.persist();
  }

  unset(key: string, options: import("./StateStore.mts").StateChangeOptions = {}) {
    super.unset(key, options);
    return this.persist();
  }
}
