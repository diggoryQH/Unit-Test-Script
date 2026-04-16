import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Province } from '../common/Province';
import { District } from '../common/District';
import { Ward } from '../common/Ward';

@Injectable({
  providedIn: 'root'
})
export class ProvinceService {
  private readonly token = '8070663b-5f69-11ef-8105-4601d6f86484';
  private readonly baseUrl = 'https://online-gateway.ghn.vn/shiip/public-api/master-data';

  constructor(private http: HttpClient) { }

  private getHeaders() {
    return new HttpHeaders({ 'Token': this.token });
  }

  getAllProvinces(): Observable<Province[]> {
    return this.http.get(`${this.baseUrl}/province`, { headers: this.getHeaders() }).pipe(
      map((res: any) => res.data.map((item: any) => ({
        code: item.ProvinceID,
        name: item.ProvinceName
      })))
    );
  }

  getDistricts(provinceId: number): Observable<District[]> {
    return this.http.get(`${this.baseUrl}/district?province_id=${provinceId}`, { headers: this.getHeaders() }).pipe(
      map((res: any) => res.data.map((item: any) => ({
        code: item.DistrictID,
        name: item.DistrictName
      })))
    );
  }

  getWards(districtId: number): Observable<Ward[]> {
    return this.http.get(`${this.baseUrl}/ward?district_id=${districtId}`, { headers: this.getHeaders() }).pipe(
      map((res: any) => res.data.map((item: any) => ({
        code: item.WardCode,
        name: item.WardName
      })))
    );
  }
}