/* eslint-env node */
const { join } = require('path');
const exec = require('child_process').spawnSync;

module.exports = {
    process(src, path) {
        const result = exec('node', [join(__dirname, 'handlebars-compiler.js'), path]);
        if(result.status) {
            throw new Error(result.stderr);
        }
        return result.stdout.toString();
    }
};
