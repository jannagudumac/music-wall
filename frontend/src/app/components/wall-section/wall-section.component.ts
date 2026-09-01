import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, HostListener, Input, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Album, Track } from '../../models/catalog.model';
import {
  CreateMusicItemRequest,
  MusicSection,
  SectionNoteColor
} from '../../models/music-wall.model';
import { MusicWallService } from '../../services/music-wall.service';
import {
  CatalogSearchComponent,
  CatalogSelection
} from '../catalog-search/catalog-search.component';
import { MusicItemComponent } from '../music-item/music-item.component';

@Component({
  selector: 'app-wall-section',
  imports: [CommonModule, FormsModule, CatalogSearchComponent, MusicItemComponent],
  templateUrl: './wall-section.component.html',
  styleUrl: './wall-section.component.css'
})
export class WallSectionComponent {

  @ViewChild('colorPickerControl') colorPickerControl?: ElementRef<HTMLElement>;
  @Input({ required: true }) wallId!: number;
  @Input({ required: true }) section!: MusicSection;
  @Output() deleted = new EventEmitter<number>();
  @Output() error = new EventEmitter<string>();

  editing = false;
  editingName = '';
  colorPickerOpen = false;
  searchOpen = false;
  saving = false;

  readonly sectionColors: { value: SectionNoteColor; label: string; hex: string }[] = [
    { value: 'CREAM', label: 'Cream', hex: '#fffbea' },
    { value: 'ROSE', label: 'Rose', hex: '#fff0f3' },
    { value: 'PEACH', label: 'Peach', hex: '#fff2df' },
    { value: 'MINT', label: 'Mint', hex: '#eef9ec' },
    { value: 'SKY', label: 'Sky', hex: '#eef7ff' },
    { value: 'LAVENDER', label: 'Lavender', hex: '#f4efff' }
  ];

  constructor(private musicWallService: MusicWallService) {
  }

  @HostListener('document:click', ['$event'])
  closeColorPickerOnOutsideClick(event: MouseEvent): void {
    const target = event.target as Node | null;
    if (
      this.colorPickerOpen &&
      target &&
      !this.colorPickerControl?.nativeElement.contains(target)
    ) {
      this.colorPickerOpen = false;
    }
  }

  startEdit(): void {
    this.editingName = this.section.name;
    this.editing = true;
  }

  saveName(): void {
    if (!this.editingName.trim()) return;
    this.updateSection(this.editingName.trim(), this.section.noteColor);
  }

  changeColor(color: SectionNoteColor): void {
    this.colorPickerOpen = false;
    this.updateSection(this.section.name, color);
  }

  deleteSection(): void {
    if (!window.confirm('Delete the section "' + this.section.name + '" and all its items?')) return;
    this.musicWallService.deleteSection(this.wallId, this.section.id).subscribe({
      next: () => this.deleted.emit(this.section.id),
      error: response => this.error.emit(response.error?.message || 'Could not delete section')
    });
  }

  addSelection(selection: CatalogSelection): void {
    const item = selection.item as Track | Album;
    if (!item.id) return;
    const request: CreateMusicItemRequest = {
      status: 'TO_LISTEN',
      catalogTrackId: selection.type === 'TRACK' ? item.id : null,
      catalogAlbumId: selection.type === 'ALBUM' ? item.id : null
    };
    this.saving = true;
    this.musicWallService.createItem(this.wallId, this.section.id, request).subscribe({
      next: created => {
        this.section.items.push(created);
        this.saving = false;
      },
      error: response => {
        this.saving = false;
        this.error.emit(response.error?.message || 'Could not add music item');
      }
    });
  }

  removeItem(itemId: number): void {
    this.section.items = this.section.items.filter(item => item.id !== itemId);
  }

  private updateSection(name: string, noteColor: SectionNoteColor): void {
    this.musicWallService.updateSection(
      this.wallId, this.section.id, { name, noteColor }
    ).subscribe({
      next: updated => {
        this.section.name = updated.name;
        this.section.noteColor = updated.noteColor;
        this.editing = false;
      },
      error: response => this.error.emit(response.error?.message || 'Could not update section')
    });
  }
}
