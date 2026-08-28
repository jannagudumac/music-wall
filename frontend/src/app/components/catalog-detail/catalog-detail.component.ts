import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Album, ArtistDetail, Track } from '../../models/catalog.model';
import { CatalogService } from '../../services/catalog.service';

@Component({
  selector: 'app-catalog-detail',
  imports: [CommonModule, RouterLink],
  templateUrl: './catalog-detail.component.html',
  styleUrl: './catalog-detail.component.css'
})
export class CatalogDetailComponent implements OnInit {

  type = '';
  artistDetail: ArtistDetail | null = null;
  album: Album | null = null;
  track: Track | null = null;
  loading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private catalogService: CatalogService
  ) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.type = params.get('type') || '';
      const rawId = params.get('id') || '';
      if (!rawId || !['artists', 'albums', 'tracks'].includes(this.type)) {
        this.errorMessage = 'This catalogue page does not exist';
        this.loading = false;
        return;
      }
      this.loadDetail(Number(rawId));
    });
  }

  get backLabel(): string {
    return this.returnWallId ? 'Back to wall' : 'Back to catalogue';
  }

  goBack(): void {
    if (this.returnWallId) {
      this.router.navigate(['/walls', this.returnWallId], {
        fragment: this.returnSectionId ? `section-${this.returnSectionId}` : undefined
      });
      return;
    }
    this.router.navigate(['/catalog']);
  }

  formatDuration(seconds: number | null): string {
    if (!seconds) return 'Duration not specified';
    return Math.floor(seconds / 60) + ':' + String(seconds % 60).padStart(2, '0');
  }

  private get returnWallId(): number | null {
    return this.numberQueryParameter('returnWallId');
  }

  private get returnSectionId(): number | null {
    return this.numberQueryParameter('returnSectionId');
  }

  private numberQueryParameter(name: string): number | null {
    const value = Number(this.route.snapshot.queryParamMap.get(name));
    return Number.isInteger(value) && value > 0 ? value : null;
  }

  private loadDetail(id: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.artistDetail = null;
    this.album = null;
    this.track = null;

    if (this.type === 'artists') {
      this.catalogService.getArtist(id).subscribe({
        next: detail => { this.artistDetail = detail; this.loading = false; },
        error: error => this.showError(error)
      });
    } else if (this.type === 'albums') {
      this.catalogService.getAlbum(id).subscribe({
        next: album => { this.album = album; this.loading = false; },
        error: error => this.showError(error)
      });
    } else {
      this.catalogService.getTrack(id).subscribe({
        next: track => { this.track = track; this.loading = false; },
        error: error => this.showError(error)
      });
    }
  }

  private showError(error: any): void {
    this.loading = false;
    this.errorMessage = error.error?.message || 'Could not load this catalogue item';
  }
}
