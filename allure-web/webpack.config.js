/*eslint-env node*/
const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin')

function makeConfig(development) {
    return {
        entry: ['./src/index.js'],
        output: {
            path: path.join(__dirname, 'build/resources/main/'),
            pathinfo: development,
            filename: 'app.js'
        },
        module: {
            loaders: [{
                test: /\.js$/,
                exclude: /node_modules/,
                loader: 'babel-loader'
            }, {
                test: /\.json$/,
                loader: 'json-loader'
            }, {
                test: /\.(png|svg|woff2?|ttf|eot)(\?.*)?$/,
                loader: 'url-loader'
            }, {
                test: /\.css$/,
                loader: development ? 'style-loader!css-loader!postcss-loader' : ExtractTextPlugin.extract('style-loader', 'css-loader!postcss-loader')
            }, {
                test: /\.hbs$/,
                loader: 'handlebars-loader',
                query: {
                    helperDirs: [
                        path.join(__dirname, 'src/helpers'),
                        path.join(__dirname, 'src/blocks')
                    ]
                }
            }]
        },
        devtool: development ? 'eval' : null,
        plugins: (() => {
            const plugins = [
                new ExtractTextPlugin('styles.css'),
                new CaseSensitivePathsPlugin()
            ];
            if(development) {
                return [...plugins, new webpack.HotModuleReplacementPlugin()];
            } else {
                return [...plugins, new webpack.optimize.UglifyJsPlugin({compress: {unsafe: true}, comments: false})];
            }
        })(),
        postcss: [
            require('postcss-import'),
            require('precss')({'import': {disable: true}}),
            require('autoprefixer')
        ]
    };
}

module.exports = makeConfig();
module.exports.factory = makeConfig;
