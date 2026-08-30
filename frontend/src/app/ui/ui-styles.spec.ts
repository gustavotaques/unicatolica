/**
 * Story 14.2 - no-hardcode SCSS guard for the base visual components.
 *
 * Reads every `src/app/ui/**\/*.scss` and fails the build (naming the file and
 * the offending declaration) on:
 *  - a hex colour literal anywhere;
 *  - a raw length literal (`px` / `rem` / `em` / `vh` / `vw` / `vmin` / `vmax` /
 *    `%`) in any sizing declaration (`font-size`, `line-height`, `padding*`,
 *    `margin*`, `gap`, `width`/`height` and their min/max, `border-radius`,
 *    `inset` / `top` / `right` / `bottom` / `left`) - these must be `var(--uc-*)`
 *    tokens from Story 14.1, optionally inside `calc()`;
 *  - a `color` / `background` / `background-color` / `border-color` value that
 *    is not a bare `var(--uc-*)` reference;
 *  - a named colour, `rgb(...)` or `hsl(...)` inside a `border` / `outline` /
 *    `box-shadow` / `fill` / `stroke` shorthand (only `var(--uc-*)`,
 *    `currentColor`, `transparent`, `none` and non-colour keywords are allowed
 *    there);
 *  - a `var(--uc-*)` reference to a token that Story 14.1's `_tokens.scss` does
 *    not declare (typo / stale rename).
 *
 * Allowed literals, per the frozen spec: `1px` / `2px` in `border` / `outline`
 * widths and offsets, `0`, and a unitless `opacity`.
 *
 * It also asserts every `.scss` file references `var(--uc-` at least once, so a
 * component styled with raw values (and zero tokens) cannot slip through.
 */
import { readdirSync, readFileSync } from 'node:fs';
import { dirname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

const uiDir = dirname(fileURLToPath(import.meta.url));

/** Every .scss file under src/app/ui, as absolute paths. */
const scssFiles = readdirSync(uiDir, { recursive: true, encoding: 'utf8' })
  .filter((entry) => entry.endsWith('.scss'))
  .map((entry) => join(uiDir, entry))
  .sort();

/** The `--uc-*` names Story 14.1 actually declares. */
const declaredTokens = new Set(
  [
    ...readFileSync(join(uiDir, '..', '..', 'styles', '_tokens.scss'), 'utf8').matchAll(
      /(--uc-[a-z0-9-]+)\s*:/g,
    ),
  ].map((m) => m[1]),
);

const stripComments = (scss: string): string =>
  scss.replace(/\/\*[\s\S]*?\*\//g, '').replace(/(^|[^:])\/\/.*$/gm, '$1');

interface Declaration {
  prop: string;
  value: string;
}

/** Split a stylesheet body into `prop: value` declarations. */
function parseDeclarations(scss: string): Declaration[] {
  const out: Declaration[] = [];
  for (const chunk of stripComments(scss).split(/[;{}]/)) {
    const m = chunk.match(/^\s*((?:--)?[a-z][a-z-]*)\s*:\s*([\s\S]+?)\s*$/i);
    if (m) out.push({ prop: m[1].toLowerCase(), value: m[2].replace(/\s+/g, ' ').trim() });
  }
  return out;
}

const HEX = /#[0-9a-fA-F]{3,8}\b/;
const LENGTH_LITERAL = /\b\d*\.?\d+(px|rem|em|vh|vw|vmin|vmax|%)\b/;
const SIZE_PROP =
  /^(font-size|line-height|border-radius|(row-|column-)?gap|(padding|margin)(-(top|right|bottom|left))?|(min-|max-)?(width|height)|inset|top|right|bottom|left)$/;
const COLOUR_PROP = /^(color|background|background-color|border-color)$/;
const BARE_UC_VAR = /^var\(\s*--uc-[a-z0-9-]+\s*\)$/;
const SHORTHAND_COLOUR_PROP = /^(border|outline|box-shadow|fill|stroke)$/;

/** Non-colour sub-tokens allowed inside a border / outline / shadow shorthand. */
const SHORTHAND_KEYWORDS = new Set([
  'solid',
  'dashed',
  'dotted',
  'double',
  'groove',
  'ridge',
  'inset',
  'outset',
  'none',
  'hidden',
  'currentcolor',
  'transparent',
  'thin',
  'medium',
  'thick',
]);

/**
 * True when a border / outline / box-shadow / fill / stroke value carries a
 * colour that is not a `var(--uc-*)` token: `rgb(...)` / `hsl(...)`, or a bare
 * word that is not a recognised non-colour keyword (e.g. `maroon`, `black`).
 * `#hex` is caught separately by the file-wide hex check.
 */
function shorthandColourOffender(value: string): boolean {
  if (/\b(rgba?|hsla?)\(/i.test(value)) return true;
  const stripped = value
    .replace(/var\(\s*--uc-[a-z0-9-]+\s*\)/gi, ' ')
    .replace(/calc\([^()]*(?:\([^()]*\)[^()]*)*\)/gi, ' ');
  for (const tok of stripped.split(/[\s,/]+/).filter(Boolean)) {
    if (/^-?\.?\d/.test(tok)) continue; // lengths / numbers
    if (/^[a-z]+$/i.test(tok) && !SHORTHAND_KEYWORDS.has(tok.toLowerCase())) return true;
  }
  return false;
}

describe('src/app/ui/**/*.scss - no hardcoded design values', () => {
  it('discovers the component stylesheets', () => {
    expect(scssFiles.length).toBeGreaterThanOrEqual(4);
  });

  it('reads the Story 14.1 token declarations from _tokens.scss', () => {
    expect(declaredTokens.size).toBeGreaterThan(10);
  });

  for (const file of scssFiles) {
    const rel = relative(uiDir, file);
    const scss = readFileSync(file, 'utf8');
    const decls = parseDeclarations(scss);

    describe(rel, () => {
      it('references at least one var(--uc-*) token', () => {
        expect(stripComments(scss)).toMatch(/var\(\s*--uc-/);
      });

      it('has no hex colour literal', () => {
        const offenders = decls
          .filter((d) => HEX.test(d.value))
          .map((d) => `${d.prop}: ${d.value}`);
        expect(offenders, `${rel} - hex literal in ${offenders.join(' | ')}`).toEqual([]);
      });

      it('uses only var(--uc-*) tokens for sizing (no px / rem / em / % literal)', () => {
        const offenders = decls
          .filter((d) => SIZE_PROP.test(d.prop) && LENGTH_LITERAL.test(d.value))
          .map((d) => `${d.prop}: ${d.value}`);
        expect(offenders, `${rel} - length literal in ${offenders.join(' | ')}`).toEqual([]);
      });

      it('uses only bare var(--uc-*) references for colour / background / border-color', () => {
        const offenders = decls
          .filter((d) => COLOUR_PROP.test(d.prop) && !BARE_UC_VAR.test(d.value))
          .map((d) => `${d.prop}: ${d.value}`);
        expect(offenders, `${rel} - non-token colour in ${offenders.join(' | ')}`).toEqual([]);
      });

      it('uses only tokens / currentColor / transparent / none for border / outline / shadow colours', () => {
        const offenders = decls
          .filter((d) => SHORTHAND_COLOUR_PROP.test(d.prop) && shorthandColourOffender(d.value))
          .map((d) => `${d.prop}: ${d.value}`);
        expect(offenders, `${rel} - hardcoded colour in ${offenders.join(' | ')}`).toEqual([]);
      });

      it('references only --uc-* tokens declared in Story 14.1', () => {
        const unknown = [
          ...new Set(
            [...stripComments(scss).matchAll(/var\(\s*(--uc-[a-z0-9-]+)\s*\)/g)].map((m) => m[1]),
          ),
        ].filter((name) => !declaredTokens.has(name));
        expect(unknown, `${rel} - unknown token(s): ${unknown.join(', ')}`).toEqual([]);
      });
    });
  }
});
