import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { MusicWallDetail, WallWallpaper } from '../../models/music-wall.model';
import { MusicWallService } from '../../services/music-wall.service';

@Component({
  selector: 'app-wall-header',
  imports: [CommonModule, FormsModule],
  templateUrl: './wall-header.component.html',
  styleUrl: './wall-header.component.css'
})
export class WallHeaderComponent {

  @Input({ required: true }) wall!: MusicWallDetail;
  @Input() isOwner = false;
  @Output() wallChanged = new EventEmitter<MusicWallDetail>();
  @Output() deleteRequested = new EventEmitter<void>();
  @Output() error = new EventEmitter<string>();

  editingField: 'name' | null = null;
  editingValue = '';
  appearanceOpen = false;

  readonly wallpaperOptions: { value: WallWallpaper; label: string }[] = [
    ...Array.from({ length: 9 }, (_, index) => ({
      value: `IMAGE_${index + 1}` as WallWallpaper,
      label: `Image ${index + 1}`
    }))
  ];

  constructor(private musicWallService: MusicWallService) {
  }

  startEdit(): void {
    if (!this.isOwner) return;
    this.editingField = 'name';
    this.editingValue = this.wall.name;
  }

  cancelEdit(): void {
    this.editingField = null;
    this.editingValue = '';
  }

  saveField(): void {
    if (!this.editingField) return;
    const value = this.editingValue.trim();
    if (this.editingField === 'name' && !value) return;
    this.musicWallService.updateWall(this.wall.id, {
      name: value,
      wallpaper: this.wall.wallpaper,
      wallColor: this.wall.wallColor
    }).subscribe({
      next: updated => {
        this.wall.name = updated.name;
        this.wallChanged.emit(this.wall);
        this.cancelEdit();
      },
      error: response => this.error.emit(response.error?.message || 'Could not update wall')
    });
  }

  changeWallpaper(wallpaper: WallWallpaper): void {
    this.saveAppearance(wallpaper, this.wall.wallColor);
  }

  selectSolidColour(): void {
    if (this.wall.wallpaper !== 'NONE') {
      this.saveAppearance('NONE', this.wall.wallColor);
    }
  }

  changeSolidColour(wallColor: string): void {
    this.saveAppearance('NONE', wallColor);
  }

  private saveAppearance(wallpaper: WallWallpaper, wallColor: string): void {
    if (!this.isOwner) return;
    this.musicWallService.updateWallAppearance(this.wall.id, { wallpaper, wallColor }).subscribe({
      next: updated => {
        this.wall.wallpaper = updated.wallpaper;
        this.wall.wallColor = updated.wallColor;
        this.wallChanged.emit(this.wall);
      },
      error: response => this.error.emit(response.error?.message || 'Could not update appearance')
    });
  }
}
