import App from './app';
import $ from 'jquery';

import './blocks/arrow/styles.css';
import './blocks/executor-icon/styles.css';

import './pluginApi';

import './plugins/default';
import './plugins/environment';
import './plugins/category';
import './plugins/xunit';
import './plugins/graph';
import './plugins/timeline';
import './plugins/history';
import './plugins/executor';
import './plugins/testrun';
import './plugins/summary';
import './plugins/severity';
import './plugins/owner';

import './plugins/testcase-info';
import './plugins/testcase-parameters';
import './plugins/testcase-description';
import './plugins/testcase-links';

App.start();

window.jQuery = $;
