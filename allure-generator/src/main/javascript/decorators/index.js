export {on} from 'backbone-decorators';

export function behavior(name, config = {}) {
    return function({prototype}) {
        prototype.behaviors = Object.assign({
            [name]: config
        }, prototype.behaviors);
    };
}

export function className(name) {
    return function(target) {
        target.prototype.className = name;
    };
}

export function tagName(tagName) {
    return function (target) {
        target.prototype.tagName = tagName;
    };
}

export function regions(regions) {
    return function (target) {
        target.prototype.regions = Object.assign(regions, target.regions);
    };
}

export function ui(ui) {
    return function (target) {
        target.prototype.ui = Object.assign(ui, target.ui);
    };
}

export function events(events) {
    return function (target) {
        target.prototype.events = Object.assign(events, target.events);
    };
}

export function modelEvents(modelEvents) {
    return function (target) {
        target.prototype.modelEvents = Object.assign(modelEvents, target.modelEvents);
    };
}

export function triggers(triggers) {
    return function (target) {
        target.prototype.triggers = Object.assign(triggers, target.triggers);
    };
}

export function childViewEvents(events) {
    return function (target) {
        target.prototype.childViewEvents = Object.assign(events, target.childViewEvents);
    };
}

export function childViewTriggers(events) {
    return function (target) {
        target.prototype.childViewTriggers = Object.assign(events, target.childViewTriggers);
    };
}

export function options(options) {
    return function (target) {
        target.prototype.options = Object.assign(options, target.options);
    };
}