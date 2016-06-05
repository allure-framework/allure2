export default function (status) {
    switch (status) {
        case 'passed': 
            return 'V';
        case 'failed':
            return '!';
        case 'broken':
            return 'X';
        case 'pending':
            return '&';
        case 'canceled':
            return '?';
        default: 
            return '';
    }
}
