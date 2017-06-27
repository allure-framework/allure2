import {values} from '../../util/statuses';

function byName(a, b) {
    return a.name.toLowerCase() < b.name.toLowerCase() ? -1 : 1;
}

function byDuration(a, b) {
    if (a.time && a.time.duration && b.time && b.time.duration) {
        return a.time.duration < b.time.duration ? -1 : 1;
    }
    return 1;
}

function byMaxDuration(a, b) {
    if (a.time && a.time.maxDuration && b.time && b.time.maxDuration) {
        return a.time.maxDuration < b.time.maxDuration ? -1 : 1;
    }
    return 1;
}

function byNodeStatus(a, b) {
    return values.indexOf(a.status) > values.indexOf(b.status) ? -1 : 1;
}

function byGroupStatuses(a, b) {
    return values.reduce((all, cur) => {
        return ((a.statistic[cur] !== b.statistic[cur]) && all === 0) ? b.statistic[cur] > a.statistic[cur] : all;
    }, 0);
}

function compare(a, b, nodeCmp, groupCmp) {
    if ('status' in a && 'statistic' in b) {
        return () => 1;
    } else if ('statistic' in a && 'status' in b) {
        return () => -1;
    } else if ('statistic' in a && 'statistic' in b) {
        return groupCmp(a, b);
    } else if ('status' in a && 'status' in b) {
        return nodeCmp(a, b);
    } else {
        return () => 0;
    }
}

export default function getComparator({sorter, ascending}) {
    const direction =  ascending ? 1 : -1;
    switch (sorter) {
        case 'sorter.name':
            return (a, b) => direction * compare(a, b, byName, byName);
        case 'sorter.duration':
            return (a, b) => direction * compare(a, b, byDuration, byMaxDuration);
        case 'sorter.status':
            return (a, b) => direction * compare(a, b, byNodeStatus, byGroupStatuses);
        default:
            return () => 0;
    }
}
