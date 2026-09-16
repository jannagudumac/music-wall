import { Injectable, signal } from '@angular/core';

// Shares the wall name with the page layout using an Angular signal.
@Injectable({ providedIn: 'root' })
export class PageHeaderService {

  // The signal updates the displayed title; null restores the normal page title.
  readonly detailTitle = signal<string | null>(null);

  show(title: string): void {
    this.detailTitle.set(title);
  }

  clear(): void {
    this.detailTitle.set(null);
  }
}
