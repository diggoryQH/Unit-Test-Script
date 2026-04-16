import { Component, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { Order } from 'src/app/common/Order';
import { OrderDetail } from 'src/app/common/OrderDetail';
import { OrderService } from 'src/app/services/order.service';
import { ReturnService } from 'src/app/services/return.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-order-detail',
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.css']
})
export class OrderDetailComponent implements OnInit {
  orderDetails!: OrderDetail[];
  order!: Order;
  returnInfo: any;
  listData!: MatTableDataSource<OrderDetail>;
  columns: string[] = ['index', 'image', 'product', 'quantity', 'price'];

  @Output() updateFinish: EventEmitter<any> = new EventEmitter<any>();
  @Input() orderId!: number;

  constructor(
    private modalService: NgbModal,
    private orderService: OrderService,
    private returnService: ReturnService,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.loadData();
  }

  // Tải lại dữ liệu mỗi khi mở hoặc cập nhật
  loadData() {
    this.orderService.getById(this.orderId).subscribe(data => {
      this.order = data as Order;
      // Nếu status là 4 (Yêu cầu trả hàng), lấy thêm thông tin từ bảng returns
      if (this.order.status === 4) {
        this.returnService.getByOrderId(this.orderId).subscribe(res => {
          this.returnInfo = res;
        });
      }
    });

    this.orderService.getByOrder(this.orderId).subscribe(data => {
      this.orderDetails = data as OrderDetail[];
      this.listData = new MatTableDataSource(this.orderDetails);
    });
  }

  open(content: TemplateRef<any>) {
    this.loadData();
    this.modalService.open(content, { centered: true, size: 'lg', backdrop: 'static' });
  }

  // Chấp nhận trả hàng (Status 4 -> 6)
  approveReturn() {
    this.process(6, 'Đã xác nhận hoàn tiền cho đơn hàng!');
  }

  // Từ chối trả hàng (Quay về Status 2 - Hoàn tất)
  rejectReturn() {
    this.process(2, 'Đã từ chối yêu cầu. Đơn hàng giữ trạng thái Hoàn tất!');
  }

  // Các logic vận hành cũ
  deliver() { this.process(1, 'Đơn hàng đã bắt đầu giao!'); }
  cancel() { this.process(3, 'Đơn hàng đã được hủy!'); }
  confirm() { this.process(2, 'Đơn hàng đã thanh toán thành công!'); }

  // Hàm xử lý chung để tái sử dụng logic SweetAlert2 và Toastr
  private process(status: number, msg: string) {
    Swal.fire({
      title: 'Xác nhận hệ thống',
      text: 'Bạn có chắc chắn muốn thay đổi trạng thái đơn hàng này?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Đồng ý',
      cancelButtonText: 'Bỏ qua'
    }).then(res => {
      if (res.isConfirmed) {
        this.orderService.updateStatus(this.orderId, status).subscribe(() => {
          this.toastr.success(msg, 'Hệ thống');
          this.updateFinish.emit('done'); // Báo hiệu cho component cha tải lại list
          this.modalService.dismissAll(); // Đóng modal
        }, error => {
          this.toastr.error('Có lỗi xảy ra khi cập nhật!', 'Hệ thống');
        });
      }
    });
  }
}