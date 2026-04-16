import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ReturnService {
  url = "http://localhost:8080/api/returns"; // Chỉnh lại theo API Backend của bạn

  constructor(private httpClient: HttpClient) { }

  // API lấy yêu cầu trả hàng theo mã đơn hàng
  getByOrderId(orderId: number) {
    return this.httpClient.get(`${this.url}/order/${orderId}`);
  }

  // API lưu yêu cầu trả hàng (cho phía khách hàng dùng)
  save(data: any) {
    return this.httpClient.post(this.url, data);
  }
}