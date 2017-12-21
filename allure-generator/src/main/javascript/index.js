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

import './plugins/testresult-block-category';
import './plugins/testresult-block-description';
import './plugins/testresult-block-duration';
import './plugins/testresult-block-execution';
import './plugins/testresult-block-links';
import './plugins/testresult-block-history';
import './plugins/testresult-block-message';
import './plugins/testresult-block-owner';
import './plugins/testresult-block-parameters';
import './plugins/testresult-block-severity';
import './plugins/testresult-block-tags';
import './plugins/testresult-tab-retries';
import './plugins/testresult-tab-history';

window.jQuery = $;

$(document).ready(() => App.start());