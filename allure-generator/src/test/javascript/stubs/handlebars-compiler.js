/* eslint-env node */
const webpack = require('webpack');
const MemoryFS = require('memory-fs');
const path = require('path');
const fs = new MemoryFS();
const file = process.argv[2];

const compiler = webpack({
    entry: file,
    output: {
        filename: 'main.js',
        libraryTarget: 'commonjs2',
    },
    target: 'node',
    module: {
        rules: [{
            test: /\.hbs$/,
            use: {
                loader: 'handlebars-loader',
                options: {
                    helperDirs: [
                        path.resolve('./src/main/javascript/helpers'),
                        path.resolve('./src/main/javascript/blocks')
                    ]
                }
            }
        }]
    },
    externals: /\.js$/,
});
compiler.outputFileSystem = fs;

compiler.run((err) => {
    if(err) {
        throw err;
    }
    console.log(fs.readFileSync(path.join(process.cwd(), 'main.js'), 'utf-8')); // eslint-disable-line no-console
});
