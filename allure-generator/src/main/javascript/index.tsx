import "react-grid-layout/css/styles.css";
import "react-resizable/css/styles.css";
import "font-awesome/css/font-awesome.min.css";
import "./styles.scss";
import * as React from "react";
import {render} from "react-dom";
import {HashRouter} from "react-router-dom";
import "./pluginApi";
import App from "./components/App";

import Overview from "./components/Overview";

window.allure.api.addReportTab({
    id: "",
    name: "Overview",
    icon: "fa fa-home",
    render: () => <Overview/>
});

const pluginsRequire = require.context("./plugins", true, /^\.\/[^\/]+\/index\.tsx$/);

pluginsRequire.keys().forEach(plugin => pluginsRequire(plugin));

render(
    <HashRouter>
        <App/>
    </HashRouter>,
    document.getElementById("app")
);
