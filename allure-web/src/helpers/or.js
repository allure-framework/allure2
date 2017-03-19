export default function(...args) {
    return !!args.slice(0, -1).reduce((a, b) => {return a || b;});
}
