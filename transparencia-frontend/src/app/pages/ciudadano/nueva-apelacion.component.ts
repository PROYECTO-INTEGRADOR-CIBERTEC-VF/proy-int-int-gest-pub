import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FileUploadComponent } from '../../components/file-upload/file-upload.component';

@Component({
  selector: 'app-nueva-apelacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, FileUploadComponent],
  templateUrl: './nueva-apelacion.component.html',
  styleUrl: './nueva-apelacion.component.css',
})
export class NuevaApelacionComponent {
  readonly documentos = signal<File[]>([]);

  onFilesChanged(files: File[]): void {
    this.documentos.set(files);
  }
}
