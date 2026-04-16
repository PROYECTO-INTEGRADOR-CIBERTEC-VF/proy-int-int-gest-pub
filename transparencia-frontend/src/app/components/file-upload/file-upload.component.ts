import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

type SlotIndex = 1 | 2 | 3;
type SlotRecord<T> = Record<SlotIndex, T>;

interface UploadSlotViewModel {
  index: SlotIndex;
  file: File | null;
  isDragging: boolean;
  error: string | null;
}

const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
const ALLOWED_EXTENSIONS = new Set(['pdf', 'jpg', 'jpeg', 'png', 'doc', 'docx']);
const ALLOWED_MIME_TYPES = new Set([
  'application/pdf',
  'image/jpeg',
  'image/png',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
]);

@Component({
  selector: 'app-file-upload',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  templateUrl: './file-upload.component.html',
  styleUrl: './file-upload.component.css',
})
export class FileUploadComponent {
  readonly maxFiles = input<number>(3);
  readonly filesChanged = output<File[]>();

  readonly file1 = signal<File | null>(null);
  readonly file2 = signal<File | null>(null);
  readonly file3 = signal<File | null>(null);

  readonly acceptTypes = '.pdf,.jpg,.jpeg,.png,.doc,.docx';

  private readonly draggingBySlot = signal<SlotRecord<boolean>>({
    1: false,
    2: false,
    3: false,
  });

  private readonly errorsBySlot = signal<SlotRecord<string | null>>({
    1: null,
    2: null,
    3: null,
  });

  readonly maxFilesSafe = computed(() => {
    const value = Number(this.maxFiles());
    if (!Number.isFinite(value)) {
      return 3;
    }
    return Math.max(1, Math.min(3, Math.floor(value)));
  });

  readonly visibleSlots = computed<SlotIndex[]>(() => {
    return [1, 2, 3].slice(0, this.maxFilesSafe()) as SlotIndex[];
  });

  readonly slotViewModels = computed<UploadSlotViewModel[]>(() => {
    const dragging = this.draggingBySlot();
    const errors = this.errorsBySlot();

    return this.visibleSlots().map((index) => ({
      index,
      file: this.getFile(index),
      isDragging: dragging[index],
      error: errors[index],
    }));
  });

  readonly uploadedFiles = computed<File[]>(() => {
    return this.visibleSlots()
      .map((slot) => this.getFile(slot))
      .filter((file): file is File => file !== null);
  });

  onDragOver(event: DragEvent, slot: SlotIndex): void {
    event.preventDefault();
    if (!this.isSlotVisible(slot)) {
      return;
    }
    this.setDragging(slot, true);
  }

  onDragLeave(event: DragEvent, slot: SlotIndex): void {
    event.preventDefault();
    this.setDragging(slot, false);
  }

  onDrop(event: DragEvent, slot: SlotIndex): void {
    event.preventDefault();
    this.setDragging(slot, false);

    if (!this.isSlotVisible(slot)) {
      return;
    }

    const droppedFile = event.dataTransfer?.files?.[0];
    if (!droppedFile) {
      return;
    }

    this.tryAssignFile(slot, droppedFile);
  }

  onSelectFile(event: Event, slot: SlotIndex): void {
    if (!this.isSlotVisible(slot)) {
      return;
    }

    const input = event.target as HTMLInputElement;
    const selectedFile = input.files?.[0];

    if (selectedFile) {
      this.tryAssignFile(slot, selectedFile);
    }

    input.value = '';
  }

  removeFile(slot: SlotIndex): void {
    this.setFile(slot, null);
    this.setError(slot, null);
    this.emitFiles();
  }

  formatFileSize(bytes: number): string {
    if (bytes >= 1024 * 1024) {
      return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
    }

    if (bytes >= 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }

    return `${bytes} B`;
  }

  private tryAssignFile(slot: SlotIndex, file: File): void {
    const validationError = this.validateFile(file);

    if (validationError) {
      this.setError(slot, validationError);
      return;
    }

    this.setError(slot, null);
    this.setFile(slot, file);
    this.emitFiles();
  }

  private validateFile(file: File): string | null {
    if (file.size > MAX_FILE_SIZE_BYTES) {
      return 'El archivo supera el limite de 10MB.';
    }

    const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
    if (!ALLOWED_EXTENSIONS.has(extension)) {
      return 'Tipo de archivo no permitido. Use PDF, JPG, PNG, DOC o DOCX.';
    }

    if (file.type && !ALLOWED_MIME_TYPES.has(file.type)) {
      return 'El tipo MIME del archivo no es valido para la carga.';
    }

    return null;
  }

  private isSlotVisible(slot: SlotIndex): boolean {
    return this.visibleSlots().includes(slot);
  }

  private setDragging(slot: SlotIndex, value: boolean): void {
    this.draggingBySlot.update((current) => ({
      ...current,
      [slot]: value,
    }));
  }

  private setError(slot: SlotIndex, value: string | null): void {
    this.errorsBySlot.update((current) => ({
      ...current,
      [slot]: value,
    }));
  }

  private setFile(slot: SlotIndex, value: File | null): void {
    if (slot === 1) {
      this.file1.set(value);
      return;
    }

    if (slot === 2) {
      this.file2.set(value);
      return;
    }

    this.file3.set(value);
  }

  private getFile(slot: SlotIndex): File | null {
    if (slot === 1) {
      return this.file1();
    }

    if (slot === 2) {
      return this.file2();
    }

    return this.file3();
  }

  private emitFiles(): void {
    this.filesChanged.emit(this.uploadedFiles());
  }
}
