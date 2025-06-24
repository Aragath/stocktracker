import { Component } from '@angular/core';
import { Router, RouterLinkActive, NavigationEnd } from '@angular/router';
import { CacheService } from '../cache.service';

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [
    RouterLinkActive,
  ],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.css'
})
export class NavBarComponent {
    currentRoute: string = '';

    constructor(private router: Router, private cacheService: CacheService) {
      this.router.events.subscribe((event) => {
        if (event instanceof NavigationEnd) {
          this.currentRoute = event.urlAfterRedirects;
        }
      });
    }

    isActive(route: string): boolean {
      return this.currentRoute.startsWith(route); 
    }

    // when search button is clicked, navigate to search page
    onSearchClick() {
      const lastSearchArg = this.cacheService.getCurrentSearchInput();
      if (lastSearchArg) {
        this.router.navigate(['/search', lastSearchArg]);
      } else {
        this.router.navigate(['/search']);
      }
    }

    // when watchlist button is clicked, navigate to watchlist page
    onWatchlistClick() {
      this.router.navigate(['/watchlist']);
    }

    // when portfolio button is clicked, navigate to portfolio page
    onPortfolioClick() {
      this.router.navigate(['/portfolio']);
    }
}
