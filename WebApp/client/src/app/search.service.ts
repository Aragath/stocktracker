// to store the last search and provide it to search component
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  private lastSearchArg: string = '';
  private lastSearchResult: any;

  setLastSearchArg(arg: string): void {
    this.lastSearchArg = arg;
  }

  getLastSearchArg(): string {
    return this.lastSearchArg;
  }
  setLastSearchResult(result: any): void {
    this.lastSearchResult = result;
  }

  getLastSearchResult(): any {
    return this.lastSearchResult;
  }

  clearLastSearch(): void {
    this.lastSearchArg = '';
    this.lastSearchResult = null;
  }
}
