module.exports = function(api) {
  api.cache(() => process.env.NODE_ENV);
  const presets = [
    "@babel/preset-env"
  ];
  const plugins = [
    ["@babel/plugin-proposal-decorators", { legacy: true }],
    "@babel/plugin-proposal-class-properties",
    "@babel/plugin-proposal-object-rest-spread",
    "@babel/plugin-transform-runtime",
  ];

  return {
    presets,
    plugins,
  };
};
