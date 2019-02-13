const webpackMerge = require("webpack-merge");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const TerserPlugin = require("terser-webpack-plugin");
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");
const sass = require("sass");

const commonConfig = require("./webpack.common.js");

const utils = require("./utils.js");

const postcssLoader = {
  loader: "postcss-loader",
  options: {
    plugins: [require("autoprefixer")()],
  },
};

const ENV = "production";

module.exports = webpackMerge(commonConfig({ env: ENV }), {
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
    minimizer: [
      new TerserPlugin({
        cache: true,
        parallel: true,
        terserOptions: {
          beautify: false,
          comments: false,
          compress: {
            warnings: false,
          },
          mangle: {
            keep_fnames: true,
          },
        },
      }),
      new OptimizeCSSAssetsPlugin({}),
    ],
  },
  plugins: [
    new MiniCssExtractPlugin({
      filename: "styles.css",
    }),
  ],
});
