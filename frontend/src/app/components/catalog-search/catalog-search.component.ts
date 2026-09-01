import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, catchError, debounceTime, distinctUntilChanged, of, switchMap, takeUntil } from 'rxjs';

import { Album, CatalogSearchResult, Track } from '../../models/catalog.model';
import { MusicItem } from '../../models/music-wall.model';
import { CatalogService } from '../../services/catalog.service';

export interface CatalogSelection {
  type: 'TRACK' | 'ALBUM';
  item: Track | Album;
}

@Component({
  selector: 'app-catalog-search',
  imports: [CommonModule, FormsModule],
  templateUrl: './catalog-search.component.html',
  styleUrl: './catalog-search.component.css'
})
export class CatalogSearchComponent implements OnInit, OnDestroy {

  @Input() existingItems: MusicItem[] = [];
  @Input() saving = false;
  @Output() selected = new EventEmitter<CatalogSelection>();
  @Output() closed = new EventEmitter<void>();

  query = '';
  tracks: Track[] = [];
  albums: Album[] = [];
  searching = false;
  message = '';

  private queryChanges = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(private catalogService: CatalogService) {
  }

  ngOnInit(): void {
    this.queryChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(query => {
        this.searching = true;
        this.message = '';
        return this.catalogService.search(query).pipe(
          catchError(error => {
            this.searching = false;
            this.message = error.error?.message || 'Could not search the catalogue';
            return of({ artists: [], albums: [], tracks: [], genres: [] });
          })
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe(result => this.showResults(result));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  queueSearch(): void {
    const cleaned = this.query.trim();
    if (cleaned.length < 2) {
      this.tracks = [];
      this.albums = [];
      this.message = cleaned ? 'Enter at least two characters.' : '';
      return;
    }
    this.queryChanges.next(cleaned);
  }

  choose(type: 'TRACK' | 'ALBUM', item: Track | Album): void {
    this.selected.emit({ type, item });
  }

  isAdded(type: 'TRACK' | 'ALBUM', id: number): boolean {
    return this.existingItems.some(item => type === 'TRACK'
      ? item.catalogTrackId === id
      : item.catalogAlbumId === id);
  }

  private showResults(result: CatalogSearchResult): void {
    this.tracks = result.tracks;
    this.albums = result.albums;
    this.searching = false;
    if (!this.tracks.length && !this.albums.length && !this.message) {
      this.message = 'No tracks or albums found in the catalogue.';
    }
  }
}
