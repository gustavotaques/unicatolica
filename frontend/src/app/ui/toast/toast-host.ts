import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

/**
 * Story 14.5 - visible toast surface.
 *
 * Reads `ToastService.toasts()` reactively (public `asReadonly()` signal, no
 * subscription/cleanup needed) and renders each queued item with a
 * fade/slide entrance (toast-host.scss). Mounted once in `app.html` so it is
 * available on every route, public and guarded alike.
 */
@Component({
  selector: 'uc-toast-host',
  templateUrl: './toast-host.html',
  styleUrl: './toast-host.scss',
})
export class UcToastHost {
  protected readonly service = inject(ToastService);
}
