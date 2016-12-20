import ssymbol from 'helpers/status-symbol.js';
import {states} from 'util/statuses.js';

describe('status symbol helper', function() {
    it('should map all statuses to symbols', function() {
        expect(states.map((state) => ssymbol(state))).not.toContain('');
    }); 
    
    it('should map unknown status to empty symbol', function() {
        expect(ssymbol('unkn')).toBe('');
    });
});
