import App from './app';
import $ from 'jquery';
import 'file-loader?name=favicon.ico!./favicon.ico';

import './elements/arrow/styles.scss';
import './elements/executor-icon/styles.scss';
import './elements/link/styles.scss';
import './elements/pane/styles.scss';
import './elements/table/styles.scss';
import './elements/tabs/styles.scss';
import './elements/widget/styles.scss';

import './pluginApi';

import './plugins/default';

import './plugins/tab-category';
import './plugins/tab-suites';
import './plugins/tab-graph';
import './plugins/tab-timeline';

import './plugins/widget-status';
import './plugins/widget-severity';
import './plugins/widget-duration';
import './plugins/widget-duration-trend';
import './plugins/widget-retry-trend';
import './plugins/widget-categories-trend';

import './plugins/widget-summary';
import './plugins/widget-history-trend';
import './plugins/widget-suites';
import './plugins/widget-categories';
import './plugins/widget-environment';
import './plugins/widget-executor';

import './plugins/testresult-description';
import './plugins/testresult-tags';
import './plugins/testresult-category';
import './plugins/testresult-history';
import './plugins/testresult-retries';
import './plugins/testresult-owner';
import './plugins/testresult-severity';
import './plugins/testresult-duration';
import './plugins/testresult-parameters';
import './plugins/testresult-links';

window.jQuery = $;

$(document).ready(() => App.start());