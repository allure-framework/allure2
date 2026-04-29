import { uniqueId } from "../runtime/ids.mts";
import { bindDelegatedEvents } from "../view/domEvents.mts";
import { attachMountable, destroyMountable, resolveMountTarget } from "../view/mountables.mts";

export default class BaseElement extends HTMLElement {
  cid: string;

  options: Record<string, unknown>;

  isAttached: boolean;

  isRendered: boolean;

  private mountedChildren: Map<
    string,
    { container: Element; mountable: import("../view/types.mts").Mountable }
  >;

  private cleanups: Set<() => void>;

  private releaseEvents: () => void;

  constructor() {
    super();
    this.cid = uniqueId("el");
    this.options = {};
    this.isAttached = false;
    this.isRendered = false;
    this.mountedChildren = new Map();
    this.cleanups = new Set();
    this.releaseEvents = () => {};
  }

  connectedCallback() {
    if (!this.isRendered) {
      this.render();
    }
    this.attachToDom();
  }

  disconnectedCallback() {
    this.detachFromDom();
  }

  setOptions(options: Record<string, unknown> = {}) {
    this.options = options;
    return this;
  }

  addCleanup(cleanup: (() => void) | null | undefined) {
    if (!cleanup) {
      return () => {};
    }

    this.cleanups.add(cleanup);

    return () => {
      if (this.cleanups.delete(cleanup)) {
        cleanup();
      }
    };
  }

  resetCleanups() {
    const cleanups = Array.from(this.cleanups);
    this.cleanups.clear();
    cleanups.forEach((cleanup) => cleanup());
    return this;
  }

  bindEvents(events: import("../view/domEvents.mts").DelegatedEvents, context: object = this) {
    this.undelegateEvents();
    this.releaseEvents = bindDelegatedEvents({
      root: this,
      events,
      context,
    });
  }

  render() {
    this.destroyMountedChildren();
    this.renderElement();
    this.isRendered = true;

    if (this.isConnected) {
      this.attachToDom();
    }

    return this;
  }

  renderElement() {
    return this;
  }

  attachToDom() {
    if (this.isAttached) {
      return;
    }

    this.isAttached = true;

    Array.from(this.mountedChildren.values()).forEach(({ mountable }) => {
      mountable.attachToDom?.();
    });
  }

  detachFromDom() {
    if (!this.isAttached) {
      return;
    }

    this.isAttached = false;

    Array.from(this.mountedChildren.values()).forEach(({ mountable }) => {
      mountable.detachFromDom?.();
    });
  }

  undelegateEvents() {
    this.releaseEvents();
    this.releaseEvents = () => {};
  }

  resolveMountTarget(target: import("../view/types.mts").MountTarget) {
    return resolveMountTarget(this, target);
  }

  mountChild(
    name: string,
    view: import("../view/types.mts").Mountable,
    target: import("../view/types.mts").MountTarget,
  ) {
    const container = this.resolveMountTarget(target);
    if (!container) {
      throw new Error(`Mount target for "${name}" is not attached to the DOM`);
    }

    this.unmountChild(name);

    this.mountedChildren.set(name, {
      container,
      mountable: view,
    });

    attachMountable(container, view);

    return view;
  }

  getMountedChild<
    TMountable extends import("../view/types.mts").Mountable =
      import("../view/types.mts").Mountable,
  >(name: string) {
    return (this.mountedChildren.get(name)?.mountable as TMountable | undefined) || null;
  }

  unmountChild(name: string) {
    const mounted = this.mountedChildren.get(name);
    if (!mounted) {
      return;
    }

    destroyMountable(mounted.mountable);
    mounted.container.replaceChildren();
    this.mountedChildren.delete(name);
  }

  destroyMountedChildren() {
    Array.from(this.mountedChildren.keys()).forEach((name) => this.unmountChild(name));
  }

  destroy() {
    this.detachFromDom();
    this.destroyMountedChildren();
    this.resetCleanups();
    this.undelegateEvents();
    this.remove();
  }
}
