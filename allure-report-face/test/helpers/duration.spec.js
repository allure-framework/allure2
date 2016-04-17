import duration from 'helpers/duration';

const SECOND = 1000;
const MINUTE = 60 * SECOND;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

describe('duration helper', function() {
    it('should format empty values as zero', function() {
        expect(duration('')).toBe('0s');
        expect(duration(0)).toBe('0s');
        expect(duration()).toBe('0s');
    });

    it('should humanize durations', function() {
        expect(duration(10)).toBe('10ms');
        expect(duration(MINUTE)).toBe('1m 00s 000ms');
        expect(duration(66 * SECOND + 1)).toBe('1m 06s 001ms');
        expect(duration(35 * MINUTE)).toBe('35m 00s 000ms');
        expect(duration(HOUR)).toBe('1h 00m 00s');
        expect(duration(5 * HOUR + 10 * MINUTE + 5 * SECOND)).toBe('5h 10m 05s');
        expect(duration(DAY)).toBe('1d 00h 00m');
        expect(duration(DAY + 5 * MINUTE)).toBe('1d 00h 05m');
    });

    it('should can limit count of signs', function() {
        expect(duration(10, 1)).toBe('10ms');
        expect(duration(MINUTE, 1)).toBe('1m');
        expect(duration(HOUR, 2)).toBe('1h 00m');
        expect(duration(HOUR, 4)).toBe('1h 00m 00s 000ms');
    });
});
