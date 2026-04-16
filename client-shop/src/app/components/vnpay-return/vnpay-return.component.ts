// src/app/vnpay-return/vnpay-return.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr'; // Đảm bảo đúng đường dẫn
import { OrderService } from 'src/app/services/order.service';

@Component({
  selector: 'app-vnpay-return',
  templateUrl: './vnpay-return.component.html'
})
export class VnpayReturnComponent implements OnInit {

  status: string = 'Đang kiểm tra giao dịch...';
  message: string = 'Vui lòng chờ trong giây lát. KHÔNG TẢI LẠI TRANG.';
  isSuccess: boolean = false;
  isLoading: boolean = true;
  
  orderInfo: string = '';
  totalPrice: string = '';
  transactionId: string = '';

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private toastr: ToastrService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const queryParams = this.route.snapshot.queryParams;

    // KIỂM TRA ĐIỀU KIỆN GỌI API: Phải có tham số bắt buộc của VNPAY (ví dụ: vnp_TxnRef)
    if (queryParams && queryParams['vnp_TxnRef']) { 
        
        // Tạo chuỗi query params chỉ khi có tham số
        const paramsString = Object.keys(queryParams)
            .map(key => key + '=' + queryParams[key])
            .join('&');

        this.checkPaymentStatus(paramsString);
        
    } else {
        // Trường hợp không có tham số VNPAY (mở trực tiếp hoặc chuyển hướng lỗi)
        this.isLoading = false;
        this.status = 'Truy cập không hợp lệ';
        this.message = 'Không tìm thấy thông tin giao dịch VNPAY.';
        this.toastr.error(this.message, 'Lỗi');
    }
}

  checkPaymentStatus(params: string) {
    this.orderService.checkPaymentVnpayStatus(params).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.orderInfo = res.vnp_OrderInfo;
        this.transactionId = res.vnp_TransactionNo;
        // Số tiền trả về từ VNPAY đã nhân 100, cần chia lại 100 để hiển thị
        this.totalPrice = (Number(res.vnp_Amount) / 100).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' }); 

        if (res.status === 'SUCCESS') {
          this.isSuccess = true;
          this.status = 'Thanh toán THÀNH CÔNG';
          this.message = res.message; 
          this.toastr.success(res.message, 'Thành công!');
          
          // GỌI THÊM HÀM CHECKOUT NẾU BẠN CHƯA GỌI Ở BACKEND
          // Ví dụ: this.completeOrder(this.orderInfo);
          
        } else {
          this.isSuccess = false;
          this.status = 'Thanh toán THẤT BẠI';
          this.message = res.message;
          this.toastr.error(res.message, 'Thất bại!');
        }
      },
      error: (err:any) => {
        this.isLoading = false;
        this.isSuccess = false;
        // Lỗi 400 Bad Request thường là Sai Chữ Ký
        const errorMessage = err.error?.message || 'Lỗi không xác định khi kiểm tra trạng thái.';
        this.status = 'LỖI HỆ THỐNG';
        this.message = errorMessage;
        this.toastr.error(errorMessage, 'Lỗi Server');
      }
    });
  }

  goToOrderHistory() {
    this.router.navigate(['/user/orders']); // Hoặc route phù hợp
  }
}