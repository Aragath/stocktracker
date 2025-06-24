import { Component, Input} from '@angular/core';
import { BackendService } from '../backend.service';
import { forkJoin, Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatAutocompleteModule} from '@angular/material/autocomplete';
import { filteredOptions} from '../search.interface'
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DetailComponent } from '../detail/detail.component';
import { Router } from '@angular/router';
import { NavBarComponent } from '../nav-bar/nav-bar.component';
import { FooterComponent } from '../footer/footer.component';
import { ActivatedRoute } from '@angular/router';
import { CacheService } from '../cache.service';
import { AutoUpdateService } from '../autoupdate.service';

@Component({
  selector: 'app-search',
  standalone: true,
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css'],
  imports: [CommonModule, 
    MatAutocompleteModule, 
    FormsModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule,
    DetailComponent,
    NavBarComponent,
    FooterComponent,
  ],
})
export class SearchComponent {
  @Input() ticker: string = "";

  private updateSubscription!: Subscription;

  searchInput: string = "";
  searchResult: any;
  options: filteredOptions[] = [];  
  autoLoading: boolean = false; // track autcomplete loading state
  searchLoading: boolean = false; // track search loading state
  showDetailComponent: boolean = false;
  marketOpen: boolean = true;
  emptyInput: boolean = false;

  constructor(private backendService: BackendService, 
              private activatedRoute: ActivatedRoute, 
              private router:Router,
              private cacheService: CacheService,
              private autoUpdateService: AutoUpdateService,
              ) {}

  ngOnInit(){
    this.subscribeToUpdateSignal(); // trigger autoupdate
    // restore search input from cache
    this.searchInput = this.cacheService.getCurrentSearchInput();
    this.activatedRoute.params.subscribe(params => {
      const ticker = params['ticker'];
      if (ticker) {
        if (ticker === this.cacheService.getCurrentSearchInput() && this.cacheService.getCurrentSearchResult()) {
          // Use the last search result without refetching
          this.searchResult = this.cacheService.getCurrentSearchResult();
          this.searchLoading = false;
          this.showDetailComponent = true;
        } else {
          this.searchInput = ticker;
          this.showDetailComponent = false;
          this.searchLoading = true;
          this.fetchDetails(ticker);
        }
      }
    });
  }

  ngOnDestroy() {
    if (this.updateSubscription) {
      this.updateSubscription.unsubscribe();
    }
  }

  subscribeToUpdateSignal() {
    this.updateSubscription = this.autoUpdateService.getUpdateSignal().subscribe(() => {
      if(this.showDetailComponent){
        this.getMarketStatus();
        if (this.marketOpen){
          this.fetchUpdates(this.cacheService.getCurrentSearchInput());
        }
      }
    });
  }

  private timeout: any = null;
  // when search bar value changes, call API
  onSearchChange(event: any) {
    const ticker = event.target.value;
    this.searchInput = ticker;
  
    // clear previous timeout
    if (this.timeout) {
      clearTimeout(this.timeout);
    }
  
    // call API after 300ms
    this.timeout = setTimeout(() => {
      if (ticker) {
        this.options = [];
        this.autoLoading = true;
        this.backendService.getAutoComplete(ticker)
          .subscribe((response) => {
            const autocompleteResponse = response as { count: number, result: filteredOptions[] };
            // filter type="Common Stock" and remove result with "." in symbol
            autocompleteResponse.result = autocompleteResponse.result.filter((option) => option.type === "Common Stock" && !option.symbol.includes("."));
            this.options = autocompleteResponse.result;
            this.autoLoading = false;
          });
      } else {
        this.options = [];
      }
    }, 300);
  }

  onOptionSelected(event: any): void {
    event.returnValue = false;
    const selectedValue = event.option.value;
    this.searchInput = selectedValue;
    // store current search input
    this.cacheService.clearCurrentSearch();
    this.cacheService.setCurrentSearchInput(this.searchInput);
    this.router.navigate(['/search', this.searchInput]);
  }

  // when user submits the form, call APIs
  onClick(event: any, inputElement: HTMLInputElement) {
    event.preventDefault();
    event.returnValue = false;
    inputElement.blur(); // remove focus on input to hide autocomplete
    // if empty input, show error message
    if (!this.searchInput) {
      this.emptyInput = true;
      return;
    }
    this.cacheService.clearCurrentSearch();
    this.cacheService.setCurrentSearchInput(this.searchInput);
    this.router.navigate(['/search', this.searchInput]);
  }

  fetchDetails(ticker: string) {
    forkJoin([
      this.backendService.getProfile(ticker),
      this.backendService.getHourly(ticker),
      this.backendService.getHistory(ticker),
      this.backendService.getQuote(ticker),
      this.backendService.getNews(ticker),
      this.backendService.getTrends(ticker),
      this.backendService.getInsider(ticker),
      this.backendService.getPeers(ticker),
      this.backendService.getEarnings(ticker),
    ]).subscribe(([d1, d2, d3, d4, d5, d6, d7, d8, d9]) => {
      console.log("profile:", d1);
      console.log("hourly:", d2);
      console.log("history:", d3);
      console.log("quote:", d4);
      console.log("news:", d5);
      console.log("trends:", d6);
      console.log("insider:", d7);
      console.log("peers:", d8);
      console.log("earnings:", d9);

      // remove peer with "." in symbol and duplicates
      let filteredPeers = (d8 as string[]).filter((peer) => !peer.includes("."));
      filteredPeers = Array.from(new Set(filteredPeers));

      this.searchResult = {
        profile: d1,
        hourly: d2,
        history: d3,
        quote: d4,
        news: d5,
        trends: d6,
        insider: d7,
        peers: filteredPeers,
        earnings: d9
      };
      this.searchLoading = false;
      this.showDetailComponent = true;
      this.cacheService.setCurrentSearchInput(ticker);
      this.cacheService.setCurrentSearchResult(this.searchResult);
    });
  }
  fetchUpdates(ticker: string) {
    forkJoin([
      this.backendService.getQuote(ticker),
    ]).subscribe(([d1]) => {
      console.log("quote:", d1);

      this.searchResult.quote = d1;

      this.searchLoading = false;
      this.showDetailComponent = true;
      this.cacheService.setCurrentSearchInput(ticker);
      this.cacheService.setCurrentSearchResult(this.searchResult);
    });
  }

  getMarketStatus() {
    const quoteTime = this.searchResult.quote.t * 1000;
    const marketCloseTime = new Date(quoteTime).getTime() + 5 * 60 *1000;
    const currentTime = new Date().getTime();
    this.marketOpen = currentTime < marketCloseTime;
  }

  // reset
  reset() {
    this.searchResult = null;
    this.options = [];
    this.showDetailComponent = false;
    this.searchInput = '';
    this.autoLoading = false;
    this.searchLoading = false;
    this.emptyInput = false;
    this.cacheService.clearCurrentSearch();
    this.cacheService.setCurrentSearchInput('');
    this.router.navigate(['/search']);
  }
}
