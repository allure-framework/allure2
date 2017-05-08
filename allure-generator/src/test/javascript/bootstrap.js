import {Behaviors, Behavior} from 'backbone.marionette';


Behaviors.behaviorsLookup  = function(options, key) {
    return { [key]: Behavior };
};

global.window.localStorage = {
    getItem() {},
    setItem() {}
};
