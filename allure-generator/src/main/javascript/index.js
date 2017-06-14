import App from './app';
import $ from 'jquery';

import 'file-loader?name=favicon.ico!./favicon.ico';

import './blocks/arrow/styles.css';
import './blocks/executor-icon/styles.css';

import './pluginApi';

import './plugins/default';

import './plugins/tab-category';
import './plugins/tab-xunit';
import './plugins/tab-graph';
import './plugins/tab-timeline';

import './plugins/widget-summary';
import './plugins/widget-history-trend';
import './plugins/widget-launch';
import './plugins/widget-executor';
import './plugins/widget-environment';

import './plugins/testcase-description';
import './plugins/testcase-tags';
import './plugins/testcase-category';
import './plugins/testcase-history';
import './plugins/testcase-retry';
import './plugins/testcase-owner';
import './plugins/testcase-severity';
import './plugins/testcase-duration';
import './plugins/testcase-parameters';
import './plugins/testcase-links';

window.jQuery = $;

$(document).ready(() => App.start());