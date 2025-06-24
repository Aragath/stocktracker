import { Component, Input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NewsData } from '../news.interface';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { faXTwitter, faFacebook} from '@fortawesome/free-brands-svg-icons';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [
    DatePipe,
    FaIconComponent,
  ],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css'
})
export class ModalComponent {
  @Input() news!: NewsData; // !: assert non-null
  icon1 = faXTwitter;
  icon2 = faFacebook;

  constructor(public activeModal: NgbActiveModal, library: FaIconLibrary){
    library.addIcons(faXTwitter);
  }

  closeModal() {
    this.activeModal.dismiss();
  }
}
