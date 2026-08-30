import { UcBadge, UcButton, UcCard, UcMemberIndicator, type UcBadgeVariant } from './index';

/**
 * Smoke-tests the barrel: a broken re-export path (a moved folder, a renamed
 * class) would fail this before any consuming epic hits it.
 */
describe('ui barrel (src/app/ui/index.ts)', () => {
  it('re-exports the four base components from their folders', () => {
    expect(UcBadge).toBeDefined();
    expect(UcButton).toBeDefined();
    expect(UcCard).toBeDefined();
    expect(UcMemberIndicator).toBeDefined();
  });

  it('re-exports the UcBadgeVariant union type', () => {
    const variants: UcBadgeVariant[] = ['course', 'open'];
    expect(variants).toEqual(['course', 'open']);
  });
});
