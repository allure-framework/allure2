export default function (status) {
    switch (status) {
        case 'passed': 
            return 'OK';
        case 'failed':
            return 'X';
        case 'broken':
            return '!';
        case 'pending':
            return '&';
        case 'canceled':
            return '?';
        default: 
            return '';
    }
}
