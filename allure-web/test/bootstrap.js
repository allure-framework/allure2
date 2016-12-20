/* eslint-env node */

//add extra modules root
process.env.NODE_PATH = 'src';
module.constructor._initPaths();

//debug log
global.dump = require('debug')('allure-face:test');

//jsdom
const jsdom = require('jsdom').jsdom;
global.document = jsdom('<html><head></head><body></body></html>', {
    url: 'http://localhost'
});
global.window = global.document.defaultView;
global.window.localStorage = jasmine.createSpyObj('localStorage', ['getItem', 'setItem']);
global.window.jQuery = require('jquery');
global.navigator = global.window.navigator;
global.location = global.window.location;

//jasmine addons
global.joc = jasmine.objectContaining;
global.jany = jasmine.any;
require('jasmine-jquery');

//require hooks
require('./handlebars-loader');
require.extensions['.css'] = function() {};
require('babel-core/register');
