import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [],
  template: `
    <footer class="footer mt-auto">
      <div class="text-center p-3" style="background-color: #DDDDDD;">
        <b>Powered by <a href="https://finnhub.io" target="_blank">Finnhub.io</a></b>
      </div>
    </footer>
  `,
  styleUrl: './footer.component.css'
})
export class FooterComponent {

}
