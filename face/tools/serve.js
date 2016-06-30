/*eslint-env node*/
const webpack = require('webpack');
const config = require('../webpack.config').factory(true);
//const express = require('express');
const WebpackDevServer = require('webpack-dev-server');
const port = process.env.PORT || 3000;

config.entry.unshift(`webpack-dev-server/client?http://localhost:${port}`, 'webpack/hot/dev-server');
const compiler = webpack(config);
const server = new WebpackDevServer(compiler, {
    contentBase: './target/allure-report',
    stats: { colors: true },
    inline: true,
    hot: true
});
server.listen(port);
