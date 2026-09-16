import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// RouterOutlet displays the current page, either login/register or the application layout.
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html'
})
export class AppComponent {
}
