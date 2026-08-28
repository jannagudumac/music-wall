import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { UserSearchResult, WallMember } from '../../models/music-wall.model';
import { MusicWallService } from '../../services/music-wall.service';

@Component({
  selector: 'app-wall-members',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './wall-members.component.html',
  styleUrl: './wall-members.component.css'
})
export class WallMembersComponent implements OnInit {

  @Input({ required: true }) wallId!: number;
  @Input({ required: true }) ownerUsername!: string;
  @Input() isOwner = false;

  members: WallMember[] = [];
  candidates: UserSearchResult[] = [];
  query = '';
  message = '';
  errorMessage = '';
  formOpen = false;
  saving = false;

  constructor(private musicWallService: MusicWallService) {
  }

  ngOnInit(): void {
    this.loadMembers();
  }

  search(): void {
    const query = this.query.trim();
    if (query.length < 2) {
      this.errorMessage = 'Enter at least two characters.';
      return;
    }
    this.errorMessage = '';
    this.musicWallService.searchMemberCandidates(this.wallId, query).subscribe({
      next: candidates => this.candidates = candidates,
      error: error => this.errorMessage = error.error?.message || 'Could not search users'
    });
  }

  add(username: string): void {
    this.saving = true;
    this.musicWallService.addMember(this.wallId, username).subscribe({
      next: member => {
        this.members.push(member);
        this.candidates = this.candidates.filter(item => item.username !== username);
        this.message = username + ' was added to the wall.';
        this.saving = false;
      },
      error: error => {
        this.errorMessage = error.error?.message || 'Could not add member';
        this.saving = false;
      }
    });
  }

  remove(member: WallMember): void {
    if (!window.confirm('Remove ' + member.username + ' from this wall?')) return;
    this.musicWallService.removeMember(this.wallId, member.username).subscribe({
      next: () => this.members = this.members.filter(item => item !== member),
      error: error => this.errorMessage = error.error?.message || 'Could not remove member'
    });
  }

  private loadMembers(): void {
    this.musicWallService.getMembers(this.wallId).subscribe({
      next: members => this.members = members,
      error: error => this.errorMessage = error.error?.message || 'Could not load members'
    });
  }
}
