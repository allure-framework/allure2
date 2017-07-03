/* eslint-env node */
const { join } = require('path');
const crypto = require('crypto');
const exec = require('child_process').spawnSync;

module.exports = {
    getCacheKey(fileData, filename, configString) {
        return crypto
            .createHash('md5')
            .update(fileData)
            .update('\0', 'utf8')
            .update(configString)
            .update('\0', 'utf8')
            .update(filename)
            .update('\0', 'utf8')
            .digest('hex');
    },

    process(src, path) {
        const result = exec('node', [join(__dirname, 'handlebars-compiler.js'), path]);
        if(result.status) {
            throw new Error(result.stderr);
        }
        return result.stdout.toString();
    }
};
