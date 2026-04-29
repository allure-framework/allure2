type HotkeyCode = "esc" | "left" | "up" | "right" | "down";
type HotkeyListener = (event: KeyboardEvent) => void;

const codes: Partial<Record<string, HotkeyCode>> = {
  Escape: "esc",
  ArrowLeft: "left",
  ArrowUp: "up",
  ArrowRight: "right",
  ArrowDown: "down",
};

class HotkeysService {
  private listeners: Map<HotkeyCode, Set<HotkeyListener>>;

  constructor() {
    this.listeners = new Map();
    this.keyHandler = this.keyHandler.bind(this);
    document.addEventListener("keydown", this.keyHandler);
  }

  subscribe(code: HotkeyCode, listener: HotkeyListener) {
    const listeners = this.listeners.get(code) || new Set<HotkeyListener>();
    listeners.add(listener);
    this.listeners.set(code, listeners);

    return () => {
      const currentListeners = this.listeners.get(code);
      if (!currentListeners) {
        return;
      }

      currentListeners.delete(listener);
      if (currentListeners.size === 0) {
        this.listeners.delete(code);
      }
    };
  }

  keyHandler(event: KeyboardEvent) {
    const code = codes[event.key];
    if (code) {
      this.listeners.get(code)?.forEach((listener) => listener(event));
    }
  }
}

export default new HotkeysService();
