import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Cart } from '../common/Cart';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  url = "http://localhost:8080/api/orders";

  urlOrderDetail = "http://localhost:8080/api/orderDetail";

  api = 'http://localhost:8080/api/vnpay';

  apiBaseUrl = 'http://localhost:8080/api';
  shipUrl = "http://localhost:8080/api/shipping/fee";

  constructor(private httpClient: HttpClient) { }

  post(email: string, cart: Cart) {
    return this.httpClient.post(this.url+'/'+email, cart);
  }

  get(email:string) {
    return this.httpClient.get(this.url+'/user/'+email);
  }

  getById(id:number) {
    return this.httpClient.get(this.url+'/'+id);
  }

  getByOrder(id:number) {
    return this.httpClient.get(this.urlOrderDetail+'/order/'+id);
  }

  cancel(id: number) {
    return this.httpClient.get(this.url+'/cancel/'+id);
  }

  // API tính phí ship từ GHN
  getShippingFee(wardCode: string, districtId: number, weight: number): Observable<number> {
    return this.httpClient.get<number>(`${this.shipUrl}?wardCode=${wardCode}&districtId=${districtId}&weight=${weight}`);
  }

  // Hàm tạo thanh toán VNPAY
  createPaymentVnpay(data: any) {
    debugger
    // URL: /api/vnpay/create-payment
    return this.httpClient.post(
      `${this.apiBaseUrl}/vnpay/create-payment`,
      data,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
  }

  // Hàm kiểm tra trạng thái thanh toán sau khi VNPAY chuyển hướng
  checkPaymentVnpayStatus(params: string) {
    debugger
    // URL: /api/vnpay/return?vnp_Amount=... (params là chuỗi query)
    return this.httpClient.get(`${this.apiBaseUrl}/vnpay/return?${params}`);
  }

// Cập nhật trạng thái đơn hàng
updateStatus(id: number, status: number): Observable<any> {
  // Giả định backend của bạn có endpoint PUT /api/orders/{id}/{status}
  // Nếu backend dùng query param hoặc body, bạn hãy điều chỉnh lại URL cho khớp
  return this.httpClient.get(`${this.url}/${id}/${status}`);
}

}
