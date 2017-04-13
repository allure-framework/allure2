import App from './app';
import $ from 'jquery';

import './blocks/arrow/styles.css';
import './blocks/executor-icon/styles.css';

import './pluginApi';

import './plugins/default';
import './plugins/environment';

import './plugins/tab-category';
import './plugins/tab-xunit';
import './plugins/tab-graph';
import './plugins/tab-timeline';

import './plugins/widget-summary';
import './plugins/widget-executor';
import './plugins/widget-launch';
import './plugins/widget-history-trend';

import './plugins/testcase-description';
import './plugins/testcase-category';
import './plugins/testcase-history';
import './plugins/testcase-retry';
import './plugins/testcase-owner';
import './plugins/testcase-severity';
import './plugins/testcase-duration';
import './plugins/testcase-parameters';
import './plugins/testcase-links';

App.start();

window.jQuery = $;
