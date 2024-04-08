const { merge } = require("webpack-merge");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const TerserPlugin = require("terser-webpack-plugin");
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const sass = require("sass");

const commonConfig = require("./webpack.common.js");

const utils = require("./utils.js");

const postcssLoader = {
  loader: "postcss-loader",
  options: {
    postcssOptions: {
      plugins: [require("autoprefixer")()],
    },
  },
};

const ENV = "production";

module.exports = merge(commonConfig({ env: ENV }), {
  mode: ENV,
  entry: {
    main: "./src/main/javascript/index",
  },
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
          {
            loader: MiniCssExtractPlugin.loader,
            options: {
              publicPath: "../",
            },
          },
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
  optimization: {
    runtimeChunk: false,
    minimizer: [new TerserPlugin(), new CssMinimizerPlugin()],
  },
  plugins: [
    new MiniCssExtractPlugin({
      filename: "styles.css",
    }),
  ],
});
