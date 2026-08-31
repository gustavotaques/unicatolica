import { Component, input } from '@angular/core';

/**
 * Story 14.2 - member indicator ("you are a member").
 *
 * Replaces the "Participar" button once the user has joined. State is conveyed
 * by the text `label` (default `Membro`) plus an inline check `<svg>` drawn in
 * `currentColor` - never by colour alone (epic-14 accessibility floor). Green
 * text (`--uc-color-green-ok`), no background (see member-indicator.scss).
 */
@Component({
  selector: 'uc-member-indicator',
  templateUrl: './member-indicator.html',
  styleUrl: './member-indicator.scss',
})
export class UcMemberIndicator {
  /**
   * Visible label. An empty / whitespace-only value falls back to `Membro` so
   * the indicator can never render as an icon + empty text - which would leave
   * the state carried by colour alone (forbidden by this story's boundary).
   */
  readonly label = input('Membro', {
    transform: (value: string): string => (value?.trim() ? value : 'Membro'),
  });
}
