import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { ChatMessage } from 'src/app/common/ChatMessage';
import { Customer } from 'src/app/common/Customer';
import { Notification } from 'src/app/common/Notification';
import { Order } from 'src/app/common/Order';
import { EditCustomerComponent } from 'src/app/components/edit-customer/edit-customer.component';
import { CustomerService } from 'src/app/services/customer.service';
import { NotificationService } from 'src/app/services/notification.service';
import { OrderService } from 'src/app/services/order.service';
import { ReturnService } from 'src/app/services/return.service';
import { SessionService } from 'src/app/services/session.service';
import { WebSocketService } from 'src/app/services/web-socket.service';
import { UploadService } from 'src/app/services/upload.service'; // Đã thêm
import Swal from 'sweetalert2';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit, OnDestroy {
  customer!: Customer;
  orders!: Order[];
  page: number = 1;
  done!: number;

  constructor(
    private customerService: CustomerService,
    private toastr: ToastrService,
    private sessionService: SessionService,
    private router: Router,
    private orderService: OrderService,
    private webSocketService: WebSocketService,
    private notificationService: NotificationService,
    private returnService: ReturnService,
    private uploadService: UploadService, // Đã tích hợp
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    this.webSocketService.openWebSocket();
    this.router.events.subscribe((evt) => {
      if (!(evt instanceof NavigationEnd)) return;
      window.scrollTo(0, 0);
    });
    this.getCustomer();
    this.getOrder();
  }

  ngOnDestroy(): void {
    this.webSocketService.closeWebSocket();
  }

  getCustomer() {
    let email = this.sessionService.getUser();
    this.customerService.getByEmail(email).subscribe(data => {
      this.customer = data as Customer;
    }, error => {
      this.toastr.error('Lỗi thông tin', 'Hệ thống');
      this.router.navigate(['/']);
    });
  }

  getOrder() {
    let email = this.sessionService.getUser();
    this.orderService.get(email).subscribe(data => {
      this.orders = data as Order[];
      this.done = this.orders.filter(o => o.status === 2).length;
    }, error => {
      this.toastr.error('Lỗi server', 'Hệ thống');
    });
  }

  async cancel(order: Order) {
    const isReturn = order.status === 2; // Status 2 là Đã nhận hàng -> Trả hàng

    const { value: formValues } = await Swal.fire({
      title: isReturn ? 'Yêu cầu Trả hàng/Hoàn tiền' : 'Yêu cầu Hủy đơn hàng',
      width: 600,
      padding: '2em',
      html: `
        <div class="text-left" style="font-size: 14px; padding: 10px;">
          <label style="font-weight: bold; display: block; margin-bottom: 5px;">Lý do:</label>
          <select id="swal-reason" class="swal2-input" style="width: 100%; margin: 0 0 15px 0;">
            ${isReturn ? `
              <option value="Sản phẩm lỗi/Hư hỏng">Sản phẩm lỗi/Hư hỏng</option>
              <option value="Giao sai hàng">Giao sai hàng</option>
              <option value="Khác với mô tả">Khác với mô tả</option>
            ` : `
              <option value="Đổi ý, không muốn mua nữa">Đổi ý, không muốn mua nữa</option>
              <option value="Tìm thấy chỗ rẻ hơn">Tìm thấy chỗ rẻ hơn</option>
              <option value="Thời gian giao hàng quá lâu">Thời gian giao hàng quá lâu</option>
            `}
          </select>
          
          <label style="font-weight: bold; display: block; margin-bottom: 5px;">Mô tả chi tiết:</label>
          <textarea id="swal-desc" class="swal2-textarea" style="width: 100%; margin: 0;" placeholder="Nhập chi tiết lý do..."></textarea>
          
          ${isReturn ? `
            <label style="font-weight: bold; display: block; margin-top: 15px; margin-bottom: 5px;">Ảnh bằng chứng (Bắt buộc):</label>
            <input type="file" id="swal-file" class="swal2-file" style="width: 100%; margin: 0;" accept="image/*">
          ` : ''}
        </div>
      `,
      showCancelButton: true,
      confirmButtonText: 'Gửi yêu cầu',
      cancelButtonText: 'Đóng',
      confirmButtonColor: '#6c63ff',
      preConfirm: () => {
        const reason = (document.getElementById('swal-reason') as HTMLSelectElement).value;
        const description = (document.getElementById('swal-desc') as HTMLTextAreaElement).value;
        const fileInput = document.getElementById('swal-file') as HTMLInputElement;

        if (!description || description.length < 10) {
          Swal.showValidationMessage('Vui lòng nhập mô tả chi tiết (tối thiểu 10 ký tự)');
          return false;
        }
        if (isReturn && (!fileInput || !fileInput.files?.[0])) {
          Swal.showValidationMessage('Vui lòng tải lên ảnh bằng chứng');
          return false;
        }

        return { reason, description, file: fileInput ? fileInput.files?.[0] : null };
      }
    });

    if (formValues) {
      Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

      let imageUrl = '';
      if (formValues.file) {
        try {
          const res = await this.uploadService.uploadCustomer(formValues.file).toPromise();
          imageUrl = res.secure_url;
        } catch (err) {
          this.toastr.error('Lỗi upload hình ảnh', 'Hệ thống');
          Swal.close();
          return;
        }
      }

      const returnRequest = {
        order: { ordersId: order.ordersId },
        reason: formValues.reason,
        description: formValues.description,
        evidenceImage: imageUrl,
        status: 0
      };

      this.returnService.save(returnRequest).subscribe({
        next: () => {
          // Sau khi lưu yêu cầu, cập nhật status đơn hàng sang 4 (Chờ duyệt)
          this.orderService.updateStatus(order.ordersId, 4).subscribe(() => {
            this.toastr.success('Yêu cầu đã được gửi, vui lòng chờ duyệt', 'Hệ thống');
            this.getOrder();
            this.sendMessage(order.ordersId);
            Swal.close();
          });
        },
        error: () => {
          this.toastr.error('Lỗi gửi yêu cầu', 'Hệ thống');
          Swal.close();
        }
      });
    }
  }

  sendMessage(id: number) {
    let chatMessage = new ChatMessage(this.customer.name, ' đã gửi yêu cầu hủy/trả đơn hàng');
    this.notificationService.post(new Notification(0, this.customer.name + ' đã gửi yêu cầu hủy/trả đơn hàng (' + id + ')')).subscribe(() => {
      this.webSocketService.sendMessage(chatMessage);
    });
  }

  finish() { this.ngOnInit(); }

  openEditCustomerModal() {
    const modalRef = this.modalService.open(EditCustomerComponent);
    modalRef.componentInstance.id = this.customer.userId;
    modalRef.componentInstance.editFinish.subscribe((result: string) => {
      if (result === 'done') this.getCustomer();
    });
  }
}