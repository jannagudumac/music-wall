import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ListeningStatus, MusicItem } from '../../models/music-wall.model';
import { MusicWallService } from '../../services/music-wall.service';

// Displays one saved item and handles its status or deletion.
@Component({
  selector: 'app-music-item',
  imports: [CommonModule, RouterLink],
  templateUrl: './music-item.component.html',
  styleUrl: './music-item.component.css'
})
export class MusicItemComponent {

  // Receive the item from the parent and report deletion or errors through @Output.
  @Input({ required: true }) wallId!: number;
  @Input({ required: true }) sectionId!: number;
  @Input({ required: true }) item!: MusicItem;
  @Output() removed = new EventEmitter<number>();
  @Output() error = new EventEmitter<string>();

  constructor(private musicWallService: MusicWallService) {
  }

  // Change only the listening status while keeping the same catalogue entry.
  toggleStatus(): void {
    const status: ListeningStatus = this.item.status === 'LISTENED'
      ? 'TO_LISTEN'
      : 'LISTENED';
    const request: Pick<MusicItem, 'status' | 'catalogTrackId' | 'catalogAlbumId'> = {
      status,
      catalogTrackId: this.item.catalogTrackId,
      catalogAlbumId: this.item.catalogAlbumId
    };
    this.musicWallService.updateItem(
      this.wallId, this.sectionId, this.item.id, request
    ).subscribe({
      next: updated => this.item.status = updated.status,
      error: response => this.error.emit(response.error?.message || 'Could not update item')
    });
  }

  // Ask for confirmation, then tell the parent to remove the item after backend success.
  deleteItem(): void {
    if (!window.confirm('Delete "' + this.item.title + '"?')) return;
    this.musicWallService.deleteItem(this.wallId, this.sectionId, this.item.id).subscribe({
      next: () => this.removed.emit(this.item.id),
      error: response => this.error.emit(response.error?.message || 'Could not delete item')
    });
  }
}
