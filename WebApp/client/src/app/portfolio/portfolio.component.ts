import { Component } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NgIf, NgFor } from '@angular/common';
import { HoldingData } from '../holding.interface';
import { QuoteData } from '../quote.interface';
import { BackendService } from '../backend.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CacheService } from '../cache.service';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { TradeModalComponent } from '../trade-modal/trade-modal.component';
import { WalletData } from '../wallet.interface';
import { Router } from '@angular/router';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [
    MatProgressSpinnerModule,
    NgIf,
    NgFor,
    CommonModule,
  ],
  templateUrl: './portfolio.component.html',
  styleUrl: './portfolio.component.css'
})
export class PortfolioComponent {
  currentWallet: WalletData = {id: 0, money: 0};
  isLoading = false;
  holdings: HoldingData[] = [];
  holdingQuote: QuoteData[] = [];
  buySuccessful: boolean = false;
  sellSuccessful: boolean = false;
  tradingTarget: string = '';

  constructor(
    private backend: BackendService,
    private cache: CacheService,
    public dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit() {
    this.fetchHoldings();
    this.getCurrentMoney();
  }

  fetchHoldings() {
    this.isLoading = true;
    this.backend.getHoldings().subscribe(data => {
      this.holdings = data as HoldingData[];
      if (this.holdings.length === 0) {
        this.isLoading = false;
        return;
      }
      this.fetchQuotesForHoldings();
    }, error => {
      console.log('Failed to fetch holdings:', error);
      this.isLoading = false;
    });
  }

  fetchQuotesForHoldings() {
    const quotesObservables = this.holdings.map(holding => {
      // get quote for each favorite
      return this.backend.getQuote(holding.ticker).pipe(
        catchError(error => {
          console.error(`Failed to fetch quote for ${holding.ticker}:`, error);
          return of(null); // returns null if error
        })
      );
    });
    // wait for all quotes
    forkJoin(quotesObservables).subscribe(quotes => {
      this.holdingQuote = quotes.filter(quote => quote !== null) as QuoteData[];
      this.isLoading = false; // Update loading state only after all quotes have been fetched
    }, error => {
      console.error('Failed to fetch quotes:', error);
      this.isLoading = false;
    });
  }
  buyStock(ticker: String, name: String, holdingQuantity: Number){
    // refetch current price
    this.backend.getQuote(ticker).subscribe((quote: any) => {
      this.openTradeModal(ticker, name, true, quote.c, holdingQuantity);
    }
    );
  }
  sellStock(ticker: String, name: String, holdingQuantity: Number){
    // refetch current price
    this.backend.getQuote(ticker).subscribe((quote: any) => {
      this.openTradeModal(ticker, name, false, quote.c, holdingQuantity);
    }
    );
  }
  openTradeModal(ticker: String, name: String, isBuying: Boolean, currentPrice: Number, holdingQuantity: Number) {
    const dialogRef = this.dialog.open(TradeModalComponent, {
      width: '400px',
      data: { 
        stockTicker: ticker, 
        stockName: name,
        isBuying: isBuying,
        currentPrice: currentPrice,
        holdingQuantity: holdingQuantity,
       }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed. Fetching updates.');
      console.log(result)
      if(result[0] === 'cancel'){
        return;
      }
      this.fetchHoldings();      
      this.getCurrentMoney();
      if(result[0] ==='buy'){
        this.buySuccessful = true;
        this.tradingTarget = result[1];
        setTimeout(() => {
          this.buySuccessful = false;
          this.tradingTarget = '';
        }, 5000);
      } else if(result[0] === 'sell'){
        this.sellSuccessful = true;
        this.tradingTarget = result[1];
        setTimeout(() => {
          this.sellSuccessful = false;
          this.tradingTarget = '';
        }, 5000);
      }
    });
  }
  getCurrentMoney() {
    this.backend.getWallet().subscribe((wallet: any) => {
      console.log("Fetching current wallet", wallet);
      this.currentWallet = wallet as WalletData;
    });
  }
  cardClick(symbol: string){
    this.router.navigate(['/search', symbol]);
  }
}
