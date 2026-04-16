// return.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ReturnService {
  url = "http://localhost:8080/api/returns";
  constructor(private http: HttpClient) { }

  save(data: any) {
    return this.http.post(this.url + '/request', data);
  }
}