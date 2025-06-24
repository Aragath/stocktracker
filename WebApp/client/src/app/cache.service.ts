// to store the last search and provide it to search component
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CacheService {
  private currentSearchInput: string = '';
  private currentSearchResult: any;
  private currentFavoriteStatus: boolean = false;
  private currentHoldingQuantity: number = 0;

  setCurrentSearchInput(input: string): void {
    this.currentSearchInput = input;
  }
  getCurrentSearchInput(): string {
    return this.currentSearchInput;
  }

  setCurrentSearchResult(result: any): void {
    this.currentSearchResult = result;
  }
  getCurrentSearchResult(): any {
    return this.currentSearchResult;
  }
  clearCurrentSearch(): void {
    this.currentSearchInput = '';
    this.currentSearchResult = null;
  }

  setCurrentHoldingQuantity(quantity: number): void{
    this.currentHoldingQuantity = quantity;
  }
  getCurrentHoldingQuantity(): number{
    return this.currentHoldingQuantity;
  }

  setCurrentFavoriteStatus(status: boolean): void {
    this.currentFavoriteStatus = status;
  }
  getCurrentFavoriteStatus(): boolean {
    return this.currentFavoriteStatus;
  }
}
