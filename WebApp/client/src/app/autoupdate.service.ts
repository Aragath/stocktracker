// to store the last search and provide it to search component
import { Injectable } from '@angular/core';
import { interval, Observable } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AutoUpdateService {
  constructor() {}

  getUpdateSignal(): Observable<number> {
    // emit value at start and every 15 seconds
    return interval(15000).pipe(startWith(0));
  }
}
