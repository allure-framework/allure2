export default function (...values) {
    values.pop();
    for(let value of values) {
        if(value) {
            return value;
        }
    }
}
