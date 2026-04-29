type DelegatedEventHandler = ((event: Event) => void) | string;

export type DelegatedEvents = Record<string, DelegatedEventHandler>;

const delegatedEventMap = {
  mouseenter: "mouseover",
  mouseleave: "mouseout",
};

const createDelegatedEvent = <TEvent extends Event>(
  event: TEvent,
  currentTarget: Element,
): TEvent =>
  new Proxy(event, {
    get(target, property) {
      if (property === "currentTarget") {
        return currentTarget;
      }

      const value = target[property as keyof TEvent];
      return typeof value === "function" ? value.bind(target) : value;
    },
  });

const shouldSkipDelegatedMouseBoundary = (eventName: string, event: Event, match: Element) => {
  if (eventName !== "mouseenter" && eventName !== "mouseleave") {
    return false;
  }

  const relatedTarget = (event as MouseEvent).relatedTarget;
  return relatedTarget instanceof Node && match.contains(relatedTarget);
};

export const bindDelegatedEvents = ({
  root,
  events,
  context,
}: {
  root: Element;
  events: DelegatedEvents;
  context: object;
}) => {
  const contextRecord = context as Record<string, unknown>;
  const delegatedEvents: { eventName: string; listener: EventListener }[] = [];

  Object.entries(events).forEach(([descriptor, handler]) => {
    const [eventName, ...selectorParts] = descriptor.split(/\s+/);
    const selector = selectorParts.join(" ");
    const method =
      typeof handler === "function"
        ? handler.bind(context)
        : typeof contextRecord[handler] === "function"
          ? contextRecord[handler].bind(context)
          : null;

    if (!method) {
      return;
    }

    const listenerEventName =
      selector && delegatedEventMap[eventName as keyof typeof delegatedEventMap]
        ? delegatedEventMap[eventName as keyof typeof delegatedEventMap]
        : eventName;

    const listener: EventListener = (event) => {
      if (!selector) {
        method(event);
        return;
      }

      const target = event.target;
      const match = target instanceof Element ? target.closest(selector) : null;
      if (!match || !root.contains(match)) {
        return;
      }

      if (shouldSkipDelegatedMouseBoundary(eventName, event, match)) {
        return;
      }

      method(createDelegatedEvent(event, match));
    };

    root.addEventListener(listenerEventName, listener);
    delegatedEvents.push({ eventName: listenerEventName, listener });
  });

  return () => {
    delegatedEvents.forEach(({ eventName, listener }) => {
      root.removeEventListener(eventName, listener);
    });
  };
};
