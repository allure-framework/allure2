/*eslint-env node*/
const fs = require('fs');
const path = require('path');
const Handlebars = require('handlebars/dist/cjs/handlebars');

const config = {
    helperDirs: [
        path.resolve(__dirname, '../src/helpers'),
        path.resolve(__dirname, '../src/blocks')
    ]
};

require.cache[require.resolve('handlebars/runtime')] = {exports: Handlebars};

Handlebars.registerHelper('helperMissing', function() {
    if(arguments.length === 1) {
        return;
    }
    const options = Array.from(arguments).pop();
    const helper = config.helperDirs.reduce(function(helper, dir) {
        return helper || require(path.join(dir, options.name));
    }, null);
    return helper.apply(this, arguments);
});

const resolvePartial = Handlebars.VM.resolvePartial;
function patchResolvePartial(filename) {
    return function(partial, content, options) {
        if(!partial) {
            partial = require(path.resolve(path.dirname(filename), options.name) + '.hbs');
        }
        return resolvePartial.call(this, partial, content, options);
    };
}

require.extensions['.hbs'] = function(module, filename) {
    Handlebars.VM.resolvePartial = patchResolvePartial(filename);
    var templateString = fs.readFileSync(filename, 'utf8');
    module.exports = Handlebars.compile(templateString);
};
