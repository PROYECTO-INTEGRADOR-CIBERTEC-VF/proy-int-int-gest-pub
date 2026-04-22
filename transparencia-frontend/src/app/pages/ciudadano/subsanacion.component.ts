import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FileUploadComponent } from '../../components/file-upload/file-upload.component';

@Component({
  selector: 'app-subsanacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, FileUploadComponent],
  templateUrl: './subsanacion.component.html',
  styleUrl: './subsanacion.component.css',
})
export class SubsanacionComponent {
  readonly adjuntos = signal<File[]>([]);

  onFilesChanged(files: File[]): void {
    this.adjuntos.set(files);
  }
}
