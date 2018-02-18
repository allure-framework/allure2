import "react-widgets/dist/css/react-widgets.css";
import "react-grid-layout/css/styles.css";
import "react-resizable/css/styles.css";
import "font-awesome/css/font-awesome.min.css";
import "./styles.scss";
import * as React from "react";
import { render } from "react-dom";
import { HashRouter } from "react-router-dom";
import "./pluginApi";
import App from "./components/App";

import Overview from "./components/Overview";
import TestResultTreeContainer from "./components/TestResultTreeContainer";

window.allure.api.addReportTab({
  id: "",
  name: "Overview",
  icon: "fa fa-home",
  render: () => <Overview />,
});

window.allure.api.addReportTab({
  id: "tree",
  name: "Tree",
  icon: "fa fa-sitemap",
  render: (id, name) => <TestResultTreeContainer name={name || id} route={id} />,
});

// const pluginsRequire = require.context("./plugins", true, /^\.\/[^\/]+\/index\.tsx$/);
// pluginsRequire.keys().forEach(plugin => pluginsRequire(plugin));

render(
  <HashRouter>
    <App />
  </HashRouter>,
  document.getElementById("app"),
);
