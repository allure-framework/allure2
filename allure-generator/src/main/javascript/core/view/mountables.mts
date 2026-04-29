const isMountTargetReference = (
  target: import("./types.mts").MountTarget,
): target is import("./types.mts").MountTargetReference => {
  return Boolean(target && typeof target === "object" && "el" in target);
};

const getMountElement = (mountable: import("./types.mts").Mountable | null | undefined) => {
  if (!mountable) {
    return null;
  }

  if (mountable instanceof Element) {
    return mountable;
  }

  if ("mountElement" in mountable && mountable.mountElement instanceof Element) {
    return mountable.mountElement;
  }

  return null;
};

export const resolveMountTarget = (root: Element, target: import("./types.mts").MountTarget) => {
  if (!target) {
    return null;
  }

  if (target instanceof Element) {
    return target;
  }

  if (typeof target === "string") {
    return root.querySelector(target);
  }

  if (isMountTargetReference(target) && target.el instanceof Element) {
    return target.el;
  }

  if (Array.isArray(target)) {
    return target[0] || null;
  }

  if (target instanceof NodeList || target instanceof HTMLCollection) {
    return target.item(0);
  }

  return null;
};

const renderMountable = <TMountable extends import("./types.mts").Mountable>(
  mountable: TMountable,
) => {
  mountable.render?.();
  return mountable;
};

export const attachMountable = <TMountable extends import("./types.mts").Mountable>(
  container: Element,
  mountable: TMountable,
) => {
  renderMountable(mountable);
  const mountElement = getMountElement(mountable);
  if (!(mountElement instanceof Element)) {
    throw new Error("Mountable has no attached element");
  }

  container.replaceChildren(mountElement);
  if (container.isConnected) {
    mountable.attachToDom?.();
  }

  return mountable;
};

export const appendMountable = <TMountable extends import("./types.mts").Mountable>(
  container: Element,
  mountable: TMountable,
) => {
  renderMountable(mountable);
  const mountElement = getMountElement(mountable);
  if (!(mountElement instanceof Element)) {
    throw new Error("Mountable has no attached element");
  }

  container.appendChild(mountElement);
  if (container.isConnected) {
    mountable.attachToDom?.();
  }

  return mountable;
};

export const destroyMountable = (mountable: import("./types.mts").Mountable | null | undefined) => {
  if (!mountable) {
    return;
  }

  if (typeof mountable.destroy === "function") {
    mountable.destroy();
    return;
  }

  getMountElement(mountable)?.remove();
};
