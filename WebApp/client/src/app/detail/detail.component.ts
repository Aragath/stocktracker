import { Component, Input, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { HighchartsChartModule } from 'highcharts-angular';
import * as Highcharts from 'highcharts/highstock';
import indicators from 'highcharts/indicators/indicators';
import vbp from 'highcharts/indicators/volume-by-price';
import { MatTabsModule } from '@angular/material/tabs';

indicators(Highcharts);
vbp(Highcharts);

import { MatDialog } from '@angular/material/dialog';
import {MatCardModule} from '@angular/material/card';
import {MatDialogModule} from '@angular/material/dialog';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalComponent } from '../news-modal/news-modal.component';
import { NewsData } from '../news.interface';

import { MdbTabsModule } from 'mdb-angular-ui-kit/tabs';
import { BackendService } from '../backend.service';
import { FavoriteData } from '../favorite.interface';
import { HoldingData } from '../holding.interface';
import { TradeModalComponent } from '../trade-modal/trade-modal.component';
import { CacheService } from '../cache.service';
import { Subscription } from 'rxjs';
import { AutoUpdateService } from '../autoupdate.service';

@Component({
  selector: 'app-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatProgressSpinnerModule,
    HighchartsChartModule,
    MatCardModule,
    MatDialogModule,
    ModalComponent,
    MdbTabsModule,
    MatTabsModule,
    TradeModalComponent,
  ],
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css'],
})
export class DetailComponent {
  @Input() showDetailComponent: boolean = false;
  @Input() searchResult: any;
  @Input() autoLoading: boolean = true;
  @Input() searchLoading: boolean = false;

  @ViewChild('modalContent') modalContent!: TemplateRef<any>;

  private updateSubscription!: Subscription;

  showError: boolean = false;
  hourlyCharts: typeof Highcharts = Highcharts;
  hourlyConstructor = "stockChart";
  hourlyOptions: Highcharts.Options = {};
  newsData: any[] = [];
  chartCharts: typeof Highcharts = Highcharts;
  chartConstructor = "stockChart";
  chartOptions: Highcharts.Options = {};
  lineColor: string = 'green';
  MSPR: number[] = [];
  CHANGE: number[] = [];
  recommendCharts: typeof Highcharts = Highcharts;
  recommendOptions: Highcharts.Options = {};
  surpriseCharts: typeof Highcharts = Highcharts;
  surpriseOptions: Highcharts.Options = {};
  marketOpen: boolean = true;
  currentTime = new Date().getTime();
  formattedCurrentTime = new Date().toLocaleString();
  formattedQuoteTime = new Date().toLocaleString();
  isFavorite: boolean = false;
  isHolding: boolean = false;
  holdingQuantity: number = 0;
  currentWallet: any;
  buySuccessful: boolean = false;
  sellSuccessful: boolean = false;
  addWatchlist: boolean = false;
  removeWatchlist: boolean = false;

  constructor(
    public dialog: MatDialog,
    private newsModalService: NgbModal,
    private backend: BackendService,
    private router: Router,
    private cache: CacheService,
    private autoUpdateService: AutoUpdateService,
    ) {
    this.getCurrentMoney();
    }

  ngOnInit() {
    this.subscribeToUpdateSignal();
    // Use the last search result without refetching
    if (this.cache.getCurrentSearchResult()) {
      this.searchResult = this.cache.getCurrentSearchResult();
      this.prepareMarketStatus();
      this.prepareHourlyData();
      this.prepareNewsData();
      this.prepareChartData();
      this.prepareInsightsData();

      // call mongodb
      this.checkFavoriteStatus();
      this.fetchHoldings();
    }
  }
  
  ngOnDestroy() {
    this.updateSubscription.unsubscribe();
  }
  // autoupdate market status and hourly data
  subscribeToUpdateSignal() {
    this.updateSubscription = this.autoUpdateService.getUpdateSignal().subscribe(() => {
      // trigger autoupdate
      if (this.showDetailComponent) {
        this.prepareMarketStatus();
        if(this.marketOpen){
          this.prepareHourlyData();
        }
      }
    });
  }
  
  // update the data when searchResult is updated
  ngOnChanges(changes: SimpleChanges) {
    if (changes['searchResult'] && !changes['searchResult'].firstChange) {
      if (!this.searchLoading){
        this.validateData();
      }
      if (this.searchResult && !this.searchLoading) {
        this.prepareMarketStatus();
        this.prepareHourlyData();
        this.prepareNewsData();
        this.prepareChartData();
        this.prepareInsightsData();
        
      }
      this.checkFavoriteStatus();
      this.fetchHoldings();
    }
  }

  prepareMarketStatus() {
    // if current time is 5 minutes after the quote timestamp, the market is closed, otherwise it is open
    const quoteTime = this.searchResult.quote.t * 1000;
    const marketCloseTime = new Date(quoteTime).getTime() + 5 * 60 *1000;
    this.currentTime = new Date().getTime();
    this.marketOpen = this.currentTime < marketCloseTime;

    let date = new Date(quoteTime);
    let year = date.getFullYear();
    let month = (date.getMonth() + 1).toString().padStart(2, '0'); // +1 because getMonth() returns month from 0-11
    let day = date.getDate().toString().padStart(2, '0');
    let hours = date.getHours().toString().padStart(2, '0');
    let minutes = date.getMinutes().toString().padStart(2, '0');
    let seconds = date.getSeconds().toString().padStart(2, '0');
    
    const formattedDate = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    this.formattedQuoteTime = formattedDate;

    date = new Date(this.currentTime);
    year = date.getFullYear();
    month = (date.getMonth() + 1).toString().padStart(2, '0'); // +1 because getMonth() returns month from 0-11
    day = date.getDate().toString().padStart(2, '0');
    hours = date.getHours().toString().padStart(2, '0');
    minutes = date.getMinutes().toString().padStart(2, '0');
    seconds = date.getSeconds().toString().padStart(2, '0');

    const formattedCurrentTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    this.formattedCurrentTime = formattedCurrentTime;
    
  }

  prepareHourlyData() {
    const data = this.searchResult.hourly;

    // market open: show stock price variation from yesterday to the today
    // market close: show stock price variation from one day before closing to the date when the market was closed.
    // get 32 data points for the chart
    const priceData: [number, number][] = [];
    for (let i = data.results.length-1; i >=0 ; i--){
      priceData.unshift([data.results[i].t, data.results[i].c]);
      if (priceData.length >= 32){
        break;
      }
    }
    if (this.searchResult.quote.d > 0){
      this.lineColor = 'green';
    }
    else{
      this.lineColor = 'red';
    }
    this.hourlyOptions = {
      colors: [this.lineColor],
      rangeSelector: {
        enabled: false
      },
      navigator: {
        enabled: false
      },
      title: {
        text: data.ticker + ' Hourly Price Variation',
        style: {
          color: 'gray',
        },
      },
      xAxis: {
        type: 'datetime',
      },
      series: [{
        name: data.ticker,
        data: priceData,
        type: 'line',
      }],
      tooltip: {
        split: true,
      },
      time: {
        useUTC: false,
        timezone: 'America/Los_Angeles'
      },
      legend: {
        enabled: false
      },
      chart: {
        backgroundColor: '#f4f4f4',
      },
    };
      
  }

  // check if any element in searchResult is empty
  private validateData() {
    this.showError = false;
    if (this.searchResult) {
      for (let key in this.searchResult) {
        const value = this.searchResult[key];
        if (Array.isArray(value) && value.length === 0) {
          this.showError = true;
          return;
        }
        if (typeof value === 'object' && Object.keys(value).length === 0) {
          this.showError = true;
          return;
        }
      }
    }
  }

  // get first 20 news without null element
  prepareNewsData() {
    const news = this.searchResult.news || [];
    const filteredNewsData = news.filter((newsItem: NewsData) => {
        return newsItem.category && 
               newsItem.datetime && 
               newsItem.headline && 
               newsItem.id && 
               newsItem.image &&
               newsItem.related && 
               newsItem.source && 
               newsItem.summary && 
               newsItem.url;
    }).slice(0, 20); // Keep only the first 20 items
    this.newsData = filteredNewsData;
  }

  // pass news data to modal component
  openModal(newsItem: NewsData) {
    const newsModalRef = this.newsModalService.open(ModalComponent);
    newsModalRef.componentInstance.news = newsItem;
  }

  // prepare chart data
  prepareChartData() {
    const data = this.searchResult.history.results;
    const ohlc = [], volume = [], dataLength = data.length
    for (let i = 0; i < dataLength; i += 1) {
        ohlc.push([
            data[i].t, // the date
            data[i].o, // open
            data[i].h, // high
            data[i].l, // low
            data[i].c // close
        ]);

        volume.push([
            data[i].t, // the date
            data[i].v // the volume
        ]);
    }

    this.chartOptions = {
        rangeSelector: {
          buttons: [{
              'type': 'month',
              'count': 1,
              'text': '1m',
          }, {
              'type': 'month',
              'count': 3,
              'text': '3m',
          }, {
              'type': 'month',
              'count': 6,
              'text': '6m',
          }, {
              'type': 'ytd',  
              'text': 'YTD',
          }, {
              'type': 'year',
              'count': 1,
              'text': '1Y',
          }, {
              'type': 'all',
              'text': 'All',
          }],
          selected: 2, // set 6m as default
        },
        title: { text: this.searchResult.history.ticker + ' Historical'},
        subtitle: { text: 'With SMA and Volume by Price technical indicators'},
        xAxis: {
          type: 'datetime'
        },
        yAxis: [{
          startOnTick: false,
          endOnTick: false,
          labels: {
              align: 'right',
              x: -3
          },
          title: {
              text: 'OHLC'
          },
          height: '60%',
          lineWidth: 2,
          resize: {
              enabled: true
          }
      }, {
          labels: {
              align: 'right',
              x: -3
          },
          title: {
              text: 'Volume'
          },
          top: '65%',
          height: '35%',
          offset: 0,
          lineWidth: 2
      }],
      tooltip: {
          split: true
      },
      chart: {
        backgroundColor: '#f4f4f4',
      },
      series: [{
          type: 'candlestick',
          name: this.searchResult.history.ticker,
          id: this.searchResult.history.ticker,
          zIndex: 2,
          data: ohlc
      }, {
          type: 'column',
          name: 'Volume',
          id: 'volume',
          data: volume,
          yAxis: 1
      }, {
          type: 'vbp',
          linkedTo: this.searchResult.history.ticker,
          params: {
              volumeSeriesID: 'volume'
          },
          dataLabels: {
              enabled: false
          },
          zoneLines: {
              enabled: false
          }
      }, {
          type: 'sma',
          linkedTo: this.searchResult.history.ticker,
          zIndex: 1,
          marker: {
              enabled: false
          }
      }],
      time: {
        useUTC: false,
        timezone: 'America/Los_Angeles'
      },
    }
  }

  prepareInsightsData(){
    // insider data
    let data = this.searchResult.insider.data;
    let mspr = 0, pos = 0, neg = 0;
    let change = 0, c_pos = 0, c_neg = 0;
    for (let i = 0; i < data.length; i++){
      mspr += data[i].mspr;
      change += data[i].change;
      if (data[i].mspr > 0){
        pos += data[i].mspr;
        c_pos += data[i].change;
      }
      else{
        neg += data[i].mspr;
        c_neg += data[i].change;
      }
      this.MSPR = [Number(mspr.toFixed(2)), Number(pos.toFixed(2)), Number(neg.toFixed(2))];
      this.CHANGE = [Number(change.toFixed(2)), Number(c_pos.toFixed(2)), Number(c_neg.toFixed(2))]
    }
    // recommend data
    data = this.searchResult.trends;
    let period = [], strongBuy = [], buy = [], hold = [], sell = [], strongSell = [];
    for (let i = 0; i < data.length; i++){
      let length = data[i].period.length;
      period.push(data[i].period.substring(0, length - 3));
      strongBuy.push(data[i].strongBuy);
      buy.push(data[i].buy);
      hold.push(data[i].hold);
      sell.push(data[i].sell);
      strongSell.push(data[i].strongSell); 
    }
    data = this.searchResult.recommend;
    this.recommendOptions = {
      chart:{
        type: 'column',
        backgroundColor: '#f4f4f4',
      },
      title: {
        text: 'Recommendation Trends'
      },
      xAxis: {
        categories: period,
        //crosshair: true
      },
      yAxis:{
        min: 0, 
        title:{
          text: '#Analysis'
        },
      },
      plotOptions: {
        column: {
            stacking: 'normal',
            dataLabels: {
                enabled: true
            }
        }
      },
      series: [{
        name: 'Strong Buy',
        data: strongBuy,
        type: 'column',
        color: 'darkgreen',
      },{
        name: 'Buy',
        data: buy,
        type: 'column',
        color: 'green',
      },{
        name: 'Hold',
        data: hold,
        type: 'column',
        color: '#B07E28',
      },{
        name: 'Sell',
        data: sell,
        type: 'column',
        color: 'red',
      },{
        name: 'Strong Sell',
        data: strongSell,
        type: 'column',
        color: 'darkred',
      }],
    }
    // surprise data
    data = this.searchResult.earnings;
    period = []
    let actual = [], estimate = [], surprise: Number[] = [];
    for (let i = 0; i < data.length; i++){
      period.push(data[i].period);
      actual.push(data[i].actual);
      estimate.push(data[i].estimate);
      surprise.push(data[i].surprise);
    }
    this.surpriseOptions = {
      chart: {
        type: 'spline',
        backgroundColor: '#f4f4f4',
      },
      title: {
        text: 'Historical EPS Surprises'
      },
      xAxis: {
        categories: period,
        // showLastLabel: true,
        labels: {
          useHTML: true,
          formatter: function () {
            let surpriseValue = surprise[this.pos];
            return '<div style="text-align: center;">' + this.value + '<br><span>Surprise: ' + surpriseValue + '</span></div>';
          }
        }
      },
      yAxis: {
        title: {
          text: 'Quantity EPS'
        },
      },
      series: [{
        name: 'Actual',
        data: actual,
        type: 'spline',
      }, {
        name: 'Estimate',
        data: estimate,
        type: 'spline',
      }]
    }
  }
  onPeerClick(peer: string){
    this.cache.clearCurrentSearch();
    this.cache.setCurrentSearchInput(peer);
    this.router.navigate(['/search', peer]);
  }
  checkFavoriteStatus() {
    this.backend.getFavorites().subscribe(favorites => {
      const favoritesArray = favorites as FavoriteData[];
      this.isFavorite = favoritesArray.some(favorite => favorite.ticker === this.searchResult.profile.ticker);
      this.cache.setCurrentFavoriteStatus(this.isFavorite);
    });
  }
  updateFavorites(ticker: string, name?: string) {
    this.backend.updateFavorites(ticker, name).subscribe(data => {
      this.isFavorite = !this.isFavorite;
      this.cache.setCurrentFavoriteStatus(this.isFavorite);
      if(this.isFavorite){
        this.addWatchlist = true;
        setTimeout(() => {
          this.addWatchlist = false;
        }, 3000);
      } else {
        this.removeWatchlist = true;
        setTimeout(() => {
          this.removeWatchlist = false;
        }, 3000);
      }
    });
  }
  fetchHoldings() {
    this.backend.getHoldings().subscribe(holdings => {
      const holdingsArray = holdings as HoldingData[];
      this.isHolding = holdingsArray.some(holding => holding.ticker === this.searchResult.profile.ticker);
      this.holdingQuantity = holdingsArray.find(holding => holding.ticker === this.searchResult.profile.ticker)?.quantity || 0;
      this.cache.setCurrentHoldingQuantity(this.holdingQuantity);
    })
  }
  getCurrentMoney() {
    this.currentWallet = this.backend.getWallet();
  }

  buyStock(){
    this.openTradeModal(true);
  }
  sellStock(){
    this.openTradeModal(false);
  }
  openTradeModal(isBuying: boolean) {
    const dialogRef = this.dialog.open(TradeModalComponent, {
      width: '400px',
      data: { 
        stockTicker: this.searchResult.profile.ticker, 
        stockName: this.searchResult.profile.name,
        isBuying: isBuying,
        currentPrice: this.searchResult.quote.c,
        holdingQuantity: this.holdingQuantity,
       }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed. Fetching updates.');
      this.fetchHoldings();
      this.getCurrentMoney();
      if(result !== 'cancel'){
        if(isBuying){
          this.buySuccessful = true;
          setTimeout(() => {
            this.buySuccessful = false;
          }, 3000);
        } else {
          this.sellSuccessful = true;
          setTimeout(() => {
            this.sellSuccessful = false;
          }, 3000);
        }
      }
    });
  }
}
