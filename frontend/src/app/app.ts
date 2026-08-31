import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { UcToastHost } from './ui/toast/toast-host';

@Component({
  imports: [RouterOutlet, UcToastHost],
  selector: 'app-root',
  templateUrl: './app.html',
})
export class App {}
