import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
@Injectable()
export class BackendService {
  readonly url = 'http://localhost:8080'
  readonly autocomplete = this.url +'/auto/'
  readonly profile = this.url +'/profile/'
  readonly hourly = this.url +'/hourly/'
  readonly history = this.url +'/history/'
  readonly quote = this.url +'/quote/'
  readonly news = this.url +'/news/'
  readonly trends = this.url +'/trends/'
  readonly insider = this.url +'/insider/'
  readonly peers = this.url +'/peers/'
  readonly earnings = this.url +'/earnings/'
  readonly favorites = this.url +'/favorites/'
  readonly holdings = this.url +'/holdings/'
  readonly wallet = this.url +'/wallet/'

  constructor(private http: HttpClient) { }

  private handleError(error: HttpErrorResponse) {
    if (error.status === 0) {
      // client-side/network error
      console.error('An error occurred:', error.error);
    } else {
      console.error(
        `Backend returned code ${error.status}, body was: `, error.error);
    }
    // user message
    return throwError(() => new Error('Something bad happened; please try again later.'));
  }

  getAutoComplete(ticker: string){
    return this.http.get(this.autocomplete + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getProfile(ticker: string){
    return this.http.get(this.profile + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getHourly(ticker: string){
    return this.http.get(this.hourly + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getHistory(ticker: string){
    return this.http.get(this.history + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getQuote(ticker: String){
    return this.http.get(this.quote + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getNews(ticker: string){
    return this.http.get(this.news + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getTrends(ticker: string){
    return this.http.get(this.trends + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getInsider(ticker: string){
    return this.http.get(this.insider + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getPeers(ticker: string){
    return this.http.get(this.peers + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getEarnings(ticker: string){
    return this.http.get(this.earnings + ticker)
      .pipe(
        catchError(this.handleError)
      );
  }
  getFavorites(){
    return this.http.get(this.favorites)
      .pipe(
        catchError(this.handleError)
      );
  }
  updateFavorites(ticker: string, name?: string){
    return this.http.post(this.favorites, { ticker: ticker, name: name }) 
      .pipe(
        catchError(this.handleError)
      );
  }
  getHoldings(){
    return this.http.get(this.holdings)
      .pipe(
        catchError(this.handleError)
      );
  }
  updateHoldings(ticker: string, name: string, quantity: number, cost: number){
    return this.http.post(this.holdings, { ticker: ticker, name: name, quantity: quantity, cost: cost }) 
      .pipe(
        catchError(this.handleError)
      );
  }
  getWallet(){
    return this.http.get(this.wallet)
      .pipe(
        catchError(this.handleError)
      );
  }
  updateWallet(change: number){
    console.log("backend service updateWallet change: ", change)
    return this.http.post(this.wallet, { change: change }) 
      .pipe(
        catchError(this.handleError)
      );
  }
}
