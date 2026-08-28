import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { MusicWallDetail } from '../../models/music-wall.model';
import { AuthService } from '../../services/auth.service';
import { MusicWallService } from '../../services/music-wall.service';
import { PageHeaderService } from '../../services/page-header.service';
import { WallHeaderComponent } from '../wall-header/wall-header.component';
import { WallMembersComponent } from '../wall-members/wall-members.component';
import { WallSectionComponent } from '../wall-section/wall-section.component';

@Component({
  selector: 'app-wall-detail',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    WallHeaderComponent,
    WallMembersComponent,
    WallSectionComponent
  ],
  templateUrl: './wall-detail.component.html',
  styleUrl: './wall-detail.component.css'
})
export class WallDetailComponent implements OnInit, OnDestroy {

  wall: MusicWallDetail | null = null;
  wallId: number;
  loading = false;
  saving = false;
  showSectionForm = false;
  errorMessage = '';
  sectionForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private musicWallService: MusicWallService,
    private pageHeaderService: PageHeaderService,
    public authService: AuthService
  ) {
    this.wallId = Number(this.route.snapshot.paramMap.get('id'));
    this.sectionForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.maxLength(80)]],
      noteColor: ['CREAM']
    });
  }

  ngOnInit(): void {
    if (!Number.isInteger(this.wallId) || this.wallId <= 0) {
      this.router.navigate(['/walls']);
      return;
    }
    this.loadWall();
  }

  ngOnDestroy(): void {
    this.pageHeaderService.clear();
  }

  get isOwner(): boolean {
    return this.wall?.ownerUsername === this.authService.getUsername();
  }

  loadWall(): void {
    this.loading = true;
    this.errorMessage = '';
    this.musicWallService.getWall(this.wallId).subscribe({
      next: wall => {
        this.wall = wall;
        this.pageHeaderService.show(wall.name);
        this.loading = false;
        this.scrollToReturnSection();
      },
      error: error => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Could not load wall';
      }
    });
  }

  createSection(): void {
    if (this.sectionForm.invalid || !this.wall) {
      this.sectionForm.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.musicWallService.createSection(this.wallId, this.sectionForm.value).subscribe({
      next: section => {
        this.wall?.sections.push(section);
        this.sectionForm.reset({ name: '', noteColor: 'CREAM' });
        this.showSectionForm = false;
        this.saving = false;
      },
      error: error => {
        this.saving = false;
        this.showError(error.error?.message || 'Could not create section');
      }
    });
  }

  deleteWall(): void {
    if (!this.wall || !window.confirm('Delete "' + this.wall.name + '" and all its contents?')) return;
    this.musicWallService.deleteWall(this.wallId).subscribe({
      next: () => this.router.navigate(['/walls']),
      error: error => this.showError(error.error?.message || 'Could not delete wall')
    });
  }

  updateWallHeader(wall: MusicWallDetail): void {
    this.wall = wall;
    this.pageHeaderService.show(wall.name);
  }

  removeSection(sectionId: number): void {
    if (this.wall) {
      this.wall.sections = this.wall.sections.filter(section => section.id !== sectionId);
    }
  }

  showError(message: string): void {
    this.errorMessage = message;
  }

  private scrollToReturnSection(): void {
    const fragment = this.route.snapshot.fragment;
    if (!fragment || !/^section-\d+$/.test(fragment)) return;
    setTimeout(() => document.getElementById(fragment)?.scrollIntoView({ block: 'center' }));
  }
}
