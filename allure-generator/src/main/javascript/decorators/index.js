export { on } from "backbone-decorators";

export function behavior(name, config = {}) {
  return function({ prototype }) {
    prototype.behaviors = Object.assign(
      {
        [name]: config,
      },
      prototype.behaviors,
    );
  };
}

export function className(name) {
  return function(target) {
    target.prototype.className = name;
  };
}

export function regions(opts) {
  return function(target) {
    target.prototype.regions = Object.assign(opts, target.regions);
  };
}

export function options(opts) {
  return function(target) {
    target.prototype.options = Object.assign(opts, target.options);
  };
}
