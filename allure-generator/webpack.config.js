/*eslint-env node*/
const path = require('path');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');

const postcssLoader = {
    loader: 'postcss-loader',
    options: {
        plugins: [
            require('autoprefixer'),
            require('postcss-rtl')
        ]
    }
};

module.exports = (env) => {
    const development = env && env.development;
    return {
        entry: ['./src/main/javascript/index.js'],
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
                test: /\.scss$/,
                use: development
                    ? ['style-loader', 'css-loader', postcssLoader, 'sass-loader']
                    : ExtractTextPlugin.extract({
                        fallback: 'style-loader',
                        use: [
                            'css-loader',
                            postcssLoader,
                            'sass-loader'
                        ]
                    })
            }, {
                test: /\.css$/,
                use: development
                    ? ['style-loader', 'css-loader']
                    : ExtractTextPlugin.extract({
                        fallback: 'style-loader',
                        use: ['css-loader']
                    })
            }, {
                test: /\.hbs$/,
                use: {
                    loader: 'handlebars-loader',
                    options: {
                        helperDirs: [
                            path.join(__dirname, 'src/main/javascript/helpers'),
                            path.join(__dirname, 'src/main/javascript/blocks')
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
            disableHostCheck: true,
            contentBase: './build/demo-report/',
            stats: {colors: true},
            host: '0.0.0.0',
            port: 3000,
            inline: true,
            hot: true
        }
    };
};
