import { Component } from '@angular/core';
import { BackendService } from '../backend.service';
import { MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { NgIf, NgFor } from '@angular/common';
import { FavoriteData } from '../favorite.interface';
import { Router } from '@angular/router';
import { QuoteData } from '../quote.interface';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CacheService } from '../cache.service';

@Component({
  selector: 'app-watchlist',
  standalone: true,
  imports: [
    MatProgressSpinnerModule,
    NgIf,
    NgFor,
  ],
  templateUrl: './watchlist.component.html',
  styleUrl: './watchlist.component.css'
})
export class WatchlistComponent {
  favorites: FavoriteData[] = [];
  favoriteQuote: QuoteData[] = [];
  isLoading: boolean = false;

  constructor(
    private backend: BackendService,
    private router: Router,
    private cache: CacheService,
    ) {}

  ngOnInit() {
    this.fetchFavorites();
  }

  fetchFavorites() {
    this.isLoading = true;
    this.backend.getFavorites().subscribe(data => {
      this.favorites = data as FavoriteData[];
      if (this.favorites.length === 0) {
        this.isLoading = false;
        return;
      }
      this.fetchQuotesForFavorites();
    }, error => {
      console.log('Failed to fetch favorites:', error);
      this.isLoading = false;
    });
  }

  fetchQuotesForFavorites() {
    const quotesObservables = this.favorites.map(favorite => {
      // get quote for each favorite
      return this.backend.getQuote(favorite.ticker).pipe(
        catchError(error => {
          console.error(`Failed to fetch quote for ${favorite.ticker}:`, error);
          return of(null); // returns null if error
        })
      );
    });
    // wait for all quotes
    forkJoin(quotesObservables).subscribe(quotes => {
      this.favoriteQuote = quotes.filter(quote => quote !== null) as QuoteData[];
      this.isLoading = false; // Update loading state only after all quotes have been fetched
    }, error => {
      console.error('Failed to fetch quotes:', error);
      this.isLoading = false;
    });
  }

  removeFromFavorites(symbol: string) {
    this.backend.updateFavorites(symbol).subscribe(() => {
      console.log('Removed favorite:', symbol);
      if(symbol === this.cache.getCurrentSearchInput()){
        this.cache.setCurrentFavoriteStatus(false);
      }
      this.fetchFavorites();
    }, error => {
      console.log('Failed to remove favorite:', symbol, error);
    });
  }

  cardClick(symbol: string){
    this.router.navigate(['/search', symbol]);
  }
}
