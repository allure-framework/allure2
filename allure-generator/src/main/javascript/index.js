import $ from "jquery";
import { App } from "./app";
import "./favicon.ico";

import "./blocks/arrow/styles.scss";
import "./blocks/executor-icon/styles.scss";
import "./blocks/status-details/styles.scss";
import "./blocks/table/styles.scss";
import "./blocks/tabs/styles.scss";
import "./blocks/pane/styles.scss";

import "./pluginApi";

import "./plugins/default";

import "./plugins/tab-category";
import "./plugins/tab-suites";
import "./plugins/tab-graph";
import "./plugins/tab-timeline";

import "./plugins/widget-status";
import "./plugins/widget-severity";
import "./plugins/widget-duration";
import "./plugins/widget-duration-trend";
import "./plugins/widget-retry-trend";
import "./plugins/widget-categories-trend";

import "./plugins/widget-summary";
import "./plugins/widget-history-trend";
import "./plugins/widget-suites";
import "./plugins/widget-categories";
import "./plugins/widget-environment";
import "./plugins/widget-executor";

import "./plugins/testresult-description";
import "./plugins/testresult-tags";
import "./plugins/testresult-category";
import "./plugins/testresult-history";
import "./plugins/testresult-retries";
import "./plugins/testresult-owner";
import "./plugins/testresult-severity";
import "./plugins/testresult-duration";
import "./plugins/testresult-parameters";
import "./plugins/testresult-links";

window.jQuery = $;

$(document).ready(() => App.start());
