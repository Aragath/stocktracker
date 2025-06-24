import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormField, MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { NgIf } from '@angular/common';
import { BackendService } from '../backend.service';
import { CacheService } from '../cache.service';
import { WalletData } from '../wallet.interface';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-trade-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatFormField,
    MatDialogModule,
    FormsModule,
    MatInputModule,
    MatFormFieldModule,
    NgIf,
  ],
  templateUrl: './trade-modal.component.html',
  styleUrl: './trade-modal.component.css'
})
export class TradeModalComponent {
  tradeQuantity = 0;
  stockTicker = '';
  stockName = '';
  isBuying = false;
  currentPrice: number = 0;
  currentWallet: WalletData = {id: 0, money: 0};
  totalCost: number = 0;
  moneyNotEnough: boolean = false;
  stockNotEnough: boolean = false;
  holdingQuantity: number = 0;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<TradeModalComponent>,
    private backendService: BackendService,
    private cacheService: CacheService
  ) {
    this.stockTicker = data.stockTicker;
    this.stockName = data.stockName;
    this.isBuying = data.isBuying;
    this.currentPrice = data.currentPrice;
    this.getCurrentWallet();
    this.holdingQuantity = data.holdingQuantity;
  }

  onTradeQuantityChange(newQuantity: number): void {
    this.tradeQuantity = newQuantity;
    this.totalCost = this.tradeQuantity * this.currentPrice;
    this.moneyNotEnough = this.currentWallet.money < this.totalCost;
    this.stockNotEnough = this.tradeQuantity > this.holdingQuantity;
  }
  
  onCloseClick(): void {
    this.dialogRef.close("cancel");
  }
  onTradeClick(): void {
    if(this.tradeQuantity === 0){
      this.dialogRef.close(["cancel", this.stockTicker]);
      return;
    }
    if(this.isBuying) {
      this.backendService.updateHoldings(this.stockTicker, this.stockName, this.tradeQuantity, this.currentPrice * this.tradeQuantity)
      .pipe(
        switchMap(result => {
          return this.backendService.updateWallet(-this.currentPrice * this.tradeQuantity);
        })
      )
      .subscribe({
        next: (result) => {
          this.dialogRef.close(["buy", this.stockTicker]);
        },
        error: (error) => {
          console.error("Buy operation failed", error);
        }
      });
    } else {
      this.backendService.updateHoldings(this.stockTicker, this.stockName, -this.tradeQuantity, this.currentPrice * this.tradeQuantity * -1)
      .pipe(
        switchMap(result => {
          return this.backendService.updateWallet(this.currentPrice * this.tradeQuantity);
        })
      )
      .subscribe({
        next: (result) => {
          this.dialogRef.close(["sell", this.stockTicker]);
        },
        error: (error) => {
          console.error("Sell operation failed", error);
        }
      });
    }
  }
  getCurrentWallet() {
    this.backendService.getWallet().subscribe((wallet: any) => {
      this.currentWallet = wallet as WalletData;
    });
  }
}
