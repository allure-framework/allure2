
function byStatuses(statuses) {
    return (child) => {
        if (child.children) {
            return child.children.length > 0;
        }
        return statuses[child.status];
    };
}

function byDuration(min, max) {
    return (child) => {
        if (child.children) {
            return child.children.length > 0;
        }
        return min <= child.time.duration && child.time.duration <= max;
    };
}


export {byStatuses, byDuration};