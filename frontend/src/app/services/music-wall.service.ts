import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import {
  MusicItem,
  MusicSection,
  MusicWall,
  MusicWallDetail,
  User
} from '../models/music-wall.model';

@Injectable({
  providedIn: 'root'
})
export class MusicWallService {

  private apiUrl = environment.apiUrl + '/walls';

  constructor(private http: HttpClient) {
  }

  getMyWalls(): Observable<MusicWall[]> {
    return this.http.get<MusicWall[]>(this.apiUrl);
  }

  createWall(request: Pick<MusicWall, 'name' | 'wallpaper' | 'wallColor'>): Observable<MusicWall> {
    return this.http.post<MusicWall>(this.apiUrl, request);
  }

  getWall(id: number): Observable<MusicWallDetail> {
    return this.http.get<MusicWallDetail>(this.apiUrl + '/' + id);
  }

  updateWall(
    id: number,
    request: Pick<MusicWall, 'name' | 'wallpaper' | 'wallColor'>
  ): Observable<MusicWall> {
    return this.http.put<MusicWall>(this.apiUrl + '/' + id, request);
  }

  updateWallAppearance(
    id: number,
    request: Pick<MusicWall, 'wallpaper' | 'wallColor'>
  ): Observable<MusicWall> {
    return this.http.put<MusicWall>(this.apiUrl + '/' + id + '/appearance', request);
  }

  deleteWall(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + id);
  }

  getMembers(wallId: number): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/${wallId}/members`);
  }

  searchMemberCandidates(wallId: number, query: string): Observable<User[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<User[]>(
      `${this.apiUrl}/${wallId}/members/search`,
      { params }
    );
  }

  addMember(wallId: number, username: string): Observable<User> {
    return this.http.post<User>(
      `${this.apiUrl}/${wallId}/members`,
      { username }
    );
  }

  removeMember(wallId: number, username: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${wallId}/members/${encodeURIComponent(username)}`
    );
  }

  createSection(
    wallId: number,
    request: Pick<MusicSection, 'name' | 'noteColor'>
  ): Observable<MusicSection> {
    return this.http.post<MusicSection>(
      this.apiUrl + '/' + wallId + '/sections',
      request
    );
  }

  updateSection(
    wallId: number,
    sectionId: number,
    request: Pick<MusicSection, 'name' | 'noteColor'>
  ): Observable<MusicSection> {
    return this.http.put<MusicSection>(
      this.apiUrl + '/' + wallId + '/sections/' + sectionId,
      request
    );
  }

  deleteSection(wallId: number, sectionId: number): Observable<void> {
    return this.http.delete<void>(
      this.apiUrl + '/' + wallId + '/sections/' + sectionId
    );
  }

  createItem(
    wallId: number,
    sectionId: number,
    request: Pick<MusicItem, 'status' | 'catalogTrackId' | 'catalogAlbumId'>
  ): Observable<MusicItem> {
    return this.http.post<MusicItem>(
      this.itemUrl(wallId, sectionId),
      request
    );
  }

  updateItem(
    wallId: number,
    sectionId: number,
    itemId: number,
    request: Pick<MusicItem, 'status' | 'catalogTrackId' | 'catalogAlbumId'>
  ): Observable<MusicItem> {
    return this.http.put<MusicItem>(
      this.itemUrl(wallId, sectionId) + '/' + itemId,
      request
    );
  }

  deleteItem(
    wallId: number,
    sectionId: number,
    itemId: number
  ): Observable<void> {
    return this.http.delete<void>(
      this.itemUrl(wallId, sectionId) + '/' + itemId
    );
  }

  private itemUrl(wallId: number, sectionId: number): string {
    return this.apiUrl + '/' + wallId + '/sections/' + sectionId + '/items';
  }
}
