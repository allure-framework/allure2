import _ from 'underscore';

const statuses = {
    failed: '#fd5a3e',
    broken: '#ffd963',
    canceled: '#ccc',
    pending: '#d35ebe',
    passed: '#97cc64'
};

const colors = _.values(statuses);
const states = _.keys(statuses);

export default {
    states, colors
};