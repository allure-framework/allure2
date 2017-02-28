/*eslint-env node*/
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');

const postcssLoader = {
    loader: 'postcss-loader',
    options: {
        plugins: [
            require('postcss-import'),
            require('precss')({'import': {disable: true}}),
            require('autoprefixer')
        ]
    }
};

module.exports = (env) => {
    const development = env && env.development;
    return {
        entry: ['./src/index.js'],
        output: {
            path: path.join(__dirname, 'build/resources/main/'),
            pathinfo: development,
            filename: 'app.js'
        },
        module: {
            rules: [{
                test: /\.js$/,
                exclude: /node_modules/,
                use: 'babel-loader'
            }, {
                test: /\.json$/,
                use: 'json-loader'
            }, {
                test: /\.(png|svg|woff2?|ttf|eot)(\?.*)?$/,
                use: 'url-loader'
            }, {
                test: /\.css$/,
                use: development
                    ? ['style-loader', 'css-loader', postcssLoader]
                    : ExtractTextPlugin.extract({
                        fallback: 'style-loader',
                        use: [
                            'css-loader',
                            postcssLoader
                        ]
                    })
            }, {
                test: /\.hbs$/,
                use: {
                    loader: 'handlebars-loader',
                    options: {
                        helperDirs: [
                            path.join(__dirname, 'src/helpers'),
                            path.join(__dirname, 'src/blocks')
                        ]
                    }
                }
            }]
        },
        devtool: development ? 'eval' : false,
        plugins: [
            new ExtractTextPlugin('styles.css'),
            new CaseSensitivePathsPlugin()
        ],
        devServer: {
            contentBase: './build/demo-report/',
            stats: {colors: true},
            port: 3000,
            inline: true,
            hot: true
        }
    };
};
