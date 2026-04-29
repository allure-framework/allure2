export type StateChangeOptions = {
  silent?: boolean;
  [key: string]: unknown;
};

type StateKey<TState> = Extract<keyof TState, string>;
type StateListener<TState extends Record<string, unknown>> = (store: StateStore<TState>) => void;
type StateKeyListener<TState extends Record<string, unknown>, TKey extends StateKey<TState>> = (
  store: StateStore<TState>,
  value: TState[TKey] | undefined,
) => void;

type StateUpdateArgs<TState extends Record<string, unknown>> =
  | [Partial<TState>, StateChangeOptions?]
  | [string, unknown, StateChangeOptions?];

const cloneValue = <TValue,>(value: TValue): TValue => {
  if (value === null || typeof value === "undefined") {
    return value;
  }

  return JSON.parse(JSON.stringify(value)) as TValue;
};

export default class StateStore<TState extends Record<string, unknown> = Record<string, unknown>> {
  state: Partial<TState>;

  previousState: Partial<TState>;

  private changeListeners: Set<StateListener<TState>>;

  private keyedChangeListeners: Map<string, Set<StateKeyListener<TState, StateKey<TState>>>>;

  constructor(initialState: Partial<TState> = {}) {
    this.state = { ...initialState };
    this.previousState = {};
    this.changeListeners = new Set();
    this.keyedChangeListeners = new Map();
  }

  get<K extends string>(key: K): K extends StateKey<TState> ? TState[K] | undefined : unknown {
    return this.state[key as unknown as StateKey<TState>] as K extends StateKey<TState>
      ? TState[K] | undefined
      : unknown;
  }

  has(key: string) {
    return Object.prototype.hasOwnProperty.call(this.state, key);
  }

  subscribe(listener: StateListener<TState>) {
    this.changeListeners.add(listener);
    return () => {
      this.changeListeners.delete(listener);
    };
  }

  subscribeKey<TKey extends StateKey<TState>>(key: TKey, listener: StateKeyListener<TState, TKey>) {
    const listeners =
      this.keyedChangeListeners.get(key) || new Set<StateKeyListener<TState, StateKey<TState>>>();
    listeners.add(listener as StateKeyListener<TState, StateKey<TState>>);
    this.keyedChangeListeners.set(key, listeners);

    return () => {
      const currentListeners = this.keyedChangeListeners.get(key);
      if (!currentListeners) {
        return;
      }

      currentListeners.delete(listener as StateKeyListener<TState, StateKey<TState>>);
      if (currentListeners.size === 0) {
        this.keyedChangeListeners.delete(key);
      }
    };
  }

  private notifyListeners(changedKeys: string[]) {
    changedKeys.forEach((name) => {
      this.keyedChangeListeners
        .get(name)
        ?.forEach((listener) =>
          listener(
            this,
            this.state[name as unknown as StateKey<TState>] as TState[StateKey<TState>] | undefined,
          ),
        );
    });

    if (changedKeys.length > 0) {
      this.changeListeners.forEach((listener) => listener(this));
    }
  }

  set(
    key: Partial<TState> | string,
    value: unknown | StateChangeOptions = undefined,
    options: StateChangeOptions = {},
  ) {
    const hasStateMap = typeof key === "object" && key !== null;
    const updates = hasStateMap ? key : ({ [key as StateKey<TState>]: value } as Partial<TState>);
    const nextOptions = hasStateMap ? (value as StateChangeOptions) || {} : options;
    const previousState = { ...this.state };
    const changedKeys: string[] = [];

    Object.entries(updates).forEach(([name, nextValue]) => {
      const stateKey = name as StateKey<TState>;

      if (previousState[stateKey] !== nextValue) {
        changedKeys.push(name);
      }

      this.state[stateKey] = nextValue as TState[typeof stateKey];
    });

    this.previousState = previousState;

    if (!nextOptions.silent && changedKeys.length > 0) {
      this.notifyListeners(changedKeys);
    }

    return this;
  }

  save(...args: StateUpdateArgs<TState>) {
    if (typeof args[0] === "string") {
      const [key, value, entryOptions] = args;
      return this.set(key, value, entryOptions);
    }

    const [updates, updateOptions] = args;
    return this.set(updates, updateOptions || {});
  }

  unset(key: string, options: StateChangeOptions = {}) {
    if (!this.has(key)) {
      return this;
    }

    this.previousState = { ...this.state };
    delete this.state[key as StateKey<TState>];

    if (!options.silent) {
      this.notifyListeners([key]);
    }

    return this;
  }

  previous<K extends string>(key: K): K extends StateKey<TState> ? TState[K] | undefined : unknown {
    return this.previousState[key as unknown as StateKey<TState>] as K extends StateKey<TState>
      ? TState[K] | undefined
      : unknown;
  }

  toJSON() {
    return cloneValue(this.state) as TState;
  }
}
