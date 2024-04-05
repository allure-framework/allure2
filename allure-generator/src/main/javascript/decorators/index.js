import { clone, has, isFunction } from "underscore";

function onDecoratorFactory(decoratorName, propertyName) {
  return function (eventName) {
    return function (target, name, descriptor) {
      if (!eventName) {
        throw new Error(`The ${decoratorName} decorator requires an eventName argument`);
      }
      if (isFunction(target[propertyName])) {
        throw new Error(
          `The ${decoratorName} decorator is not compatible with ${propertyName} as method form`,
        );
      }
      if (target[propertyName] && !has(target, propertyName)) {
        target[propertyName] = clone(target[propertyName]);
      }
      if (!target[propertyName]) {
        target[propertyName] = {};
      }
      target[propertyName][eventName] = name;
      return descriptor;
    };
  };
}

export const on = onDecoratorFactory("on", "events");

export function behavior(name, config = {}) {
  return function (target) {
    target.prototype.behaviors = Object.assign(
      {
        [name]: config,
      },
      target.prototype.behaviors,
    );
  };
}

export function className(name) {
  return function (target) {
    target.prototype.className = name;
  };
}

export function regions(opts) {
  return function (target) {
    target.prototype.regions = Object.assign(opts, target.regions);
  };
}

export function options(opts) {
  return function (target) {
    target.prototype.options = Object.assign(opts, target.options);
  };
}
