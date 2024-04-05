const webpack = require("webpack");
const { merge } = require("webpack-merge");
const sass = require("sass");
const utils = require("./utils.js");

const commonConfig = require("./webpack.common.js");

const ENV = "development";

const postcssLoader = {
  loader: "postcss-loader",
  options: {
    postcssOptions: {
      plugins: [require("autoprefixer")()],
    },
  },
};

module.exports = (options) =>
  merge(commonConfig({ env: ENV }), {
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
      hot: true,
      static: "./build/demo-report",
      historyApiFallback: true,
      watchFiles: ["./src/main/javascript"],
    },
  });
