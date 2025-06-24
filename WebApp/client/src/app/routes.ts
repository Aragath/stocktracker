import { Routes } from '@angular/router';
import { SearchComponent } from './search/search.component';
import { WatchlistComponent } from './watchlist/watchlist.component';
import { PortfolioComponent } from './portfolio/portfolio.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'search/home',
    pathMatch: 'full'
  },
  {
    path: 'search',
    redirectTo: 'search/home',
    pathMatch: 'full'
  },
  {
    path: 'search/home',
    component: SearchComponent
  },
  {
    path: 'search/:ticker',
    component: SearchComponent
  },
  {
    path: 'watchlist',
    component: WatchlistComponent
  },
  {
    path: 'portfolio',
    component: PortfolioComponent
  }
];
export default routes;