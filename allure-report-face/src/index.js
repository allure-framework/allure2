import App from './app';
import $ from 'jquery';
import Backbone from 'backbone';
import './plugins/default';
import './plugins/defects';
import './plugins/environment';
import './plugins/xunit';
import './plugins/behaviors';
import './plugins/graph';
import './plugins/timeline';
import './plugins/report-stats';

import './plugins/testcase-parameters';
import './plugins/testcase-description';
import './plugins/testcase-links';

App.start();
Backbone.history.start();

window.jQuery = $;
