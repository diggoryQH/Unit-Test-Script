import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UploadService {

  constructor(private http: HttpClient) {}

  uploadProduct(file: File): Observable<any> {
    const data = new FormData();
    data.append('file', file);
    data.append('upload_preset', environment.UPLOAD_PRESET);

    return this.http.post(environment.CLOUDINARY_UPLOAD_URL, data);
  }

  uploadCustomer(file: File): Observable<any> {
    const data = new FormData();
    data.append('file', file);
    data.append('upload_preset', environment.UPLOAD_PRESET);

    return this.http.post(environment.CLOUDINARY_UPLOAD_URL, data);
  }
}
