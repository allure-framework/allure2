# Feature Demo: Expand/Collapse All Nested Steps

## Before vs After

### Before (Original)
```
┌─────────────────────────────────────────────┐
│ Execution                                   │
├─────────────────────────────────────────────┤
│ ▶ Test body                                 │
│   ▶ Step 1                                  │
│   ▶ Step 2 (with nested steps)              │
│   ▶ Step 3                                  │
└─────────────────────────────────────────────┘
```
*Users had to click each step individually to expand*

### After (With New Feature)
```
┌─────────────────────────────────────────────┐
│ Execution          [Expand all / Collapse all] │
├─────────────────────────────────────────────┤
│ ▼ Test body                                 │
│   ▼ Step 1                                  │
│     Details of step 1                       │
│   ▼ Step 2 (with nested steps)              │
│     ▼ Nested step 2.1                       │
│       Details of nested step 2.1            │
│     ▼ Nested step 2.2                       │
│       Details of nested step 2.2            │
│   ▼ Step 3                                  │
│     Details of step 3                       │
└─────────────────────────────────────────────┘
```
*Users can now expand/collapse all steps with one click*

## UI Components

### Header Layout
```
┌──────────────────────────────────────────────────────┐
│ [Execution Title]           [Expand all / Collapse all] │
└──────────────────────────────────────────────────────┘
     ↑                                    ↑
     Fixed title                    New controls
```

### Controls Interaction
1. **Expand all**: One click → All nested steps expand
2. **Collapse all**: One click → All nested steps collapse
3. **Individual clicks**: Still works for fine-grained control

## Code Structure

### Event Flow
```
User clicks "Expand all"
    ↓
onExpandAllClick() event handler triggered
    ↓
Query all steps with class ".step__title_hasContent"
    ↓
Add ".step_expanded" class to parent elements
    ↓
CSS shows nested content (step__content)
    ↓
All nested steps now visible
```

### CSS Class Logic
```scss
.step {
  &__content {
    display: none;  // Hidden by default
  }
  
  &_expanded > &__content {
    display: block;  // Shown when parent has .step_expanded
  }
}
```

## Usage Scenarios

### Scenario 1: Debugging Complex Test Failures
**Problem**: Test has 10+ nested steps, need to see all to understand failure
**Solution**: Click "Expand all" → See entire execution flow instantly

### Scenario 2: Reviewing Test Coverage
**Problem**: Need to collapse everything to see high-level structure
**Solution**: Click "Collapse all" → Get clean overview of main steps

### Scenario 3: Mixed Approach
**Problem**: Want most steps collapsed but some expanded
**Solution**: 
1. Click "Collapse all"
2. Manually expand specific steps of interest
3. Individual clicks still work normally

## Internationalization

The feature is fully internationalized with translations for:
- 🇬🇧 English: "Expand all / Collapse all"
- 🇪🇸 Spanish: "Expandir todo / Colapsar todo"
- 🇷🇺 Russian: "Развернуть все / Свернуть все"
- 🇩🇪 German: "Alle ausklappen / Alle einklappen"
- 🇫🇷 French: "Tout déplier / Tout plier"
- 🇨🇳 Chinese: "全部展开 / 全部折叠"
- 🇯🇵 Japanese: "すべて展開 / すべて折りたたむ"

## Browser Compatibility

Works in all browsers supported by Allure:
- ✅ Chrome/Edge (Chromium)
- ✅ Firefox
- ✅ Safari
- ✅ Mobile browsers

## Performance

- **Fast**: Uses CSS classes, no DOM manipulation beyond class changes
- **Efficient**: Query selector runs once per click
- **Scalable**: Works equally well with 10 steps or 1000 steps

## Accessibility

- Controls are keyboard accessible (clickable links)
- Screen reader friendly (semantic HTML)
- Visual indicators (arrow icons) for expand/collapse state
- Works with keyboard navigation

## Edge Cases Handled

1. **No nested steps**: Controls only appear when content exists
2. **Already expanded**: "Expand all" is idempotent (safe to click multiple times)
3. **Stage sections**: Works within Test body, Set up, and Tear down sections
4. **Deep nesting**: Works at any nesting level (recursive)
