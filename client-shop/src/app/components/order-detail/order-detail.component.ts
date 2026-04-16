import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { Order } from 'src/app/common/Order';
import { OrderDetail } from 'src/app/common/OrderDetail';
import { OrderService } from 'src/app/services/order.service';
import { ReturnService } from 'src/app/services/return.service';
import { UploadService } from 'src/app/services/upload.service';

import Swal from 'sweetalert2';

@Component({
  selector: 'app-order-detail',
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.css']
})
export class OrderDetailComponent implements OnInit {
  orderDetails!: OrderDetail[];
  order!: Order;

  @Input() id!: number;

  constructor(
    private modalService: NgbModal,
    private orderService: OrderService,
    private returnService: ReturnService, // Đã tích hợp
    private uploadService: UploadService, // Đã tích hợp từ Admin
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.getOrder();
    this.getItems();
  }

  getOrder() {
    this.orderService.getById(this.id).subscribe(data => {
      this.order = data as Order;
    }, error => this.toastr.error('Lỗi server', 'Hệ thống'));
  }

  getItems() {
    this.orderService.getByOrder(this.id).subscribe(data => {
      this.orderDetails = data as OrderDetail[];
    });
  }

  open(content: TemplateRef<any>) {
    this.modalService.open(content, { centered: true, size: 'lg' });
  }

  // Hàm xử lý Hủy/Trả hàng linh hoạt
  async handleCancelOrReturn() {
    const isReturnMode = this.order.status === 2; // Nếu status=2 là Trả hàng, ngược lại là Hủy
    
    const { value: formValues } = await Swal.fire({
      title: isReturnMode ? 'Yêu cầu Trả hàng & Hoàn tiền' : 'Yêu cầu Hủy đơn hàng',
      html: `
        <div style="text-align: left;">
          <label class="font-weight-bold">Lý do:</label>
          <select id="swal-reason" class="swal2-input" style="width: 100%; margin: 10px 0;">
            <option value="Đổi ý, không muốn mua nữa">Đổi ý, không muốn mua nữa</option>
            <option value="Sản phẩm lỗi/hư hỏng">Sản phẩm lỗi/hư hỏng</option>
            <option value="Giao sai hàng/thiếu hàng">Giao sai hàng/thiếu hàng</option>
            <option value="Tìm thấy chỗ rẻ hơn">Tìm thấy chỗ rẻ hơn</option>
          </select>
          <label class="font-weight-bold">Mô tả chi tiết:</label>
          <textarea id="swal-desc" class="swal2-textarea" style="width: 100%; margin: 10px 0;" placeholder="Mô tả tình trạng chi tiết..."></textarea>
          ${isReturnMode ? `
            <label class="font-weight-bold">Ảnh bằng chứng (Bắt buộc):</label>
            <input type="file" id="swal-file" class="swal2-file" style="width: 100%;" accept="image/*">
          ` : ''}
        </div>
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: 'Gửi yêu cầu',
      cancelButtonText: 'Bỏ qua',
      preConfirm: () => {
        const reason = (document.getElementById('swal-reason') as HTMLSelectElement).value;
        const description = (document.getElementById('swal-desc') as HTMLTextAreaElement).value;
        const fileInput = document.getElementById('swal-file') as HTMLInputElement;

        if (!description) {
          Swal.showValidationMessage('Vui lòng nhập mô tả chi tiết');
          return false;
        }
        if (isReturnMode && (!fileInput || !fileInput.files?.[0])) {
          Swal.showValidationMessage('Vui lòng chọn ảnh bằng chứng');
          return false;
        }

        return {
          reason: reason,
          description: description,
          file: fileInput ? fileInput.files?.[0] : null
        };
      }
    });

    if (formValues) {
      Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
      
      let evidenceUrl = '';

      // Chỉ upload ảnh nếu ở chế độ Trả hàng và có file
      if (isReturnMode && formValues.file) {
        try {
          const res = await this.uploadService.uploadCustomer(formValues.file).toPromise();
          evidenceUrl = res.secure_url;
        } catch (error) {
          this.toastr.error('Upload ảnh thất bại');
          Swal.close();
          return;
        }
      }

      const requestBody = {
        order: { ordersId: this.order.ordersId },
        reason: formValues.reason,
        description: formValues.description,
        evidenceImage: evidenceUrl,
        status: 0 // Yêu cầu mới
      };

      this.returnService.save(requestBody).subscribe(() => {
        this.toastr.success('Yêu cầu đã được gửi thành công!');
        this.orderService.updateStatus(this.order.ordersId, 4).subscribe(); // Cập nhật đơn hàng sang status 4 (Chờ duyệt)
        this.getOrder();
        this.modalService.dismissAll();
        Swal.close();
      });
    }
  }

  finish() { this.ngOnInit(); }
}