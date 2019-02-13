const webpack = require("webpack");

const utils = require("./utils.js");

module.exports = options => ({
  cache: options.env !== "production",
  resolve: {
    extensions: [".js", ".json"],
    alias: {
      app: utils.root("src/main/javascript"),
    },
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        include: [utils.root("src/main/javascript")],
        exclude: /node_modules/,
        use: "babel-loader",
      },
      {
        test: /\.(png|svg|woff2?|ttf|eot|gif)(\?.*)?$/,
        use: "url-loader",
      },
      {
        test: /\.(ico)(\?.*)?$/,
        loader: "file-loader?name=[name].ico",
      },
      {
        test: /\.hbs$/,
        use: {
          loader: "handlebars-loader",
          options: {
            helperDirs: [
              utils.root("src/main/javascript/helpers"),
              utils.root("src/main/javascript/blocks"),
            ],
          },
        },
      },
    ],
  },
  stats: {
    children: false,
  },
  plugins: [
    new webpack.DefinePlugin({
      "process.env": {
        NODE_ENV: `'${options.env}'`,
        DEBUG_INFO_ENABLED: options.env === "development",
      },
    }),
  ],
});
