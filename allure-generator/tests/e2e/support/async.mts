export const createDeferredGate = () => {
  let release = () => {};
  let markSeen = () => {};
  let seenOnce = false;
  const wait = new Promise<void>((resolve) => {
    release = resolve;
  });
  const seen = new Promise<void>((resolve) => {
    markSeen = () => {
      if (seenOnce) {
        return;
      }

      seenOnce = true;
      resolve();
    };
  });

  return { release, wait, seen, markSeen };
};
