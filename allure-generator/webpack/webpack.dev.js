const webpack = require("webpack");
const webpackMerge = require("webpack-merge");
const sass = require("sass");
const utils = require("./utils.js");

const commonConfig = require("./webpack.common.js");

const ENV = "development";

const postcssLoader = {
  loader: "postcss-loader",
  options: {
    plugins: [require("autoprefixer")()],
  },
};

module.exports = options =>
  webpackMerge(commonConfig({ env: ENV }), {
    devtool: "cheap-module-source-map", // https://reactjs.org/docs/cross-origin-errors.html
    mode: ENV,
    entry: ["./src/main/javascript/index.js"],
    output: {
      path: utils.root("build/www"),
      publicPath: "/",
      filename: "app.js",
    },
    module: {
      rules: [
        {
          test: /\.(sa|sc|c)ss$/,
          use: [
            "style-loader",
            "css-loader",
            postcssLoader,
            {
              loader: "sass-loader",
              options: { implementation: sass },
            },
          ],
        },
      ],
    },
    devServer: {
      stats: options.stats,
      hot: true,
      contentBase: "./build/demo-report",
      historyApiFallback: true,
      watchOptions: {
        ignored: /node_modules/,
      },
    },
    plugins: [new webpack.HotModuleReplacementPlugin()],
  });
