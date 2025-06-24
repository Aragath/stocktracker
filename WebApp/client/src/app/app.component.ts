import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RouterModule } from '@angular/router';
import { NavBarComponent } from './nav-bar/nav-bar.component';
import { FooterComponent } from './footer/footer.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet, 
    RouterModule,
    NavBarComponent,
    FooterComponent,
  ],
  template: `
    <main>
      <app-nav-bar></app-nav-bar>
      <section class="content p-0">
        <router-outlet></router-outlet>
      </section>
      <app-footer></app-footer>
    </main>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
}

