import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NavigationEnd, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Cart } from 'src/app/common/Cart';
import { CartDetail } from 'src/app/common/CartDetail';
import { ChatMessage } from 'src/app/common/ChatMessage';
import { District } from 'src/app/common/District';
import { Notification } from 'src/app/common/Notification';
import { Order } from 'src/app/common/Order';
import { Province } from 'src/app/common/Province';
import { Ward } from 'src/app/common/Ward';
import { CartService } from 'src/app/services/cart.service';
import { NotificationService } from 'src/app/services/notification.service';
import { OrderService } from 'src/app/services/order.service';
import { ProvinceService } from 'src/app/services/province.service';
import { SessionService } from 'src/app/services/session.service';
import { WebSocketService } from 'src/app/services/web-socket.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {

  cart!: Cart;
  cartDetails!: CartDetail[];

  discount: number = 0;
  amount: number = 0; // Tổng tiền hàng sau giảm giá
  amountReal: number = 0; // Tổng tiền gốc
  
  shippingFee: number = 0;
  totalWeight: number = 0;
  totalPay: number = 0; // Tổng cuối cùng = amount + shippingFee

  postForm: FormGroup;
  provinces!: Province[];
  districts!: District[];
  wards!: Ward[];

  // Lưu trữ object đã chọn để lấy 'name' nối chuỗi địa chỉ
  provinceSelected!: Province;
  districtSelected!: District;
  wardSelected!: Ward;

  provinceCode!: number;
  districtCode!: number;
  wardCode!: any; // WardCode của GHN có thể là string

  constructor(
    private cartService: CartService,
    private toastr: ToastrService,
    private router: Router,
    private sessionService: SessionService,
    private orderService: OrderService,
    private location: ProvinceService,
    private webSocketService: WebSocketService,
    private notificationService: NotificationService) {
    this.postForm = new FormGroup({
      'phone': new FormControl(null, [Validators.required, Validators.pattern('(0)[0-9]{9}')]),
      'province': new FormControl(0, [Validators.required, Validators.min(1)]),
      'district': new FormControl(0, [Validators.required, Validators.min(1)]),
      'ward': new FormControl(0, [Validators.required, Validators.min(1)]),
      'number': new FormControl('', Validators.required),
    })
  }

  ngOnInit(): void {
    this.webSocketService.openWebSocket();
    this.router.events.subscribe((evt) => {
      if (!(evt instanceof NavigationEnd)) return;
      window.scrollTo(0, 0)
    });
    this.getAllItem();
    this.getProvinces();
  }

  getAllItem() {
    let email = this.sessionService.getUser();
    this.cartService.getCart(email).subscribe(data => {
      this.cart = data as Cart;
      this.postForm.patchValue({ 'phone': this.cart.phone });
      
      this.cartService.getAllDetail(this.cart.cartId).subscribe(data => {
        this.cartDetails = data as CartDetail[];
        this.cartService.setLength(this.cartDetails.length);
        
        if (this.cartDetails.length == 0) {
          this.router.navigate(['/']);
          this.toastr.info('Giỏ hàng trống!', 'Hệ thống');
          return;
        }

        this.amount = 0;
        this.amountReal = 0;
        this.totalWeight = 0;

        this.cartDetails.forEach(item => {
          this.amountReal += item.product.price * item.quantity;
          this.amount += item.price;
          // GHN tính theo Gram. Nếu product.weight là KG thì * 1000, nếu đã là Gram thì giữ nguyên.
          this.totalWeight += (item.product.weight * 1000 || 200) * item.quantity;
        });
        this.discount = this.amountReal - this.amount;
        this.updateTotal();
      });
    });
  }

  updateTotal() {
    this.totalPay = this.amount + this.shippingFee;
  }

  // --- HÀM TÍNH PHÍ SHIP ---
  calculateShipping() {
    if (this.wardCode && this.districtCode && this.totalWeight > 0) {
      this.orderService.getShippingFee(this.wardCode.toString(), this.districtCode, this.totalWeight)
        .subscribe(fee => {
          this.shippingFee = fee;
          this.updateTotal();
        }, () => {
          this.toastr.error('Lỗi tính phí vận chuyển từ GHN', 'Hệ thống');
          this.shippingFee = 0;
          this.updateTotal();
        });
    }
  }

  // --- LOGIC ĐỊA CHỈ (MAPPED CODE/NAME) ---
  getProvinces() {
    this.location.getAllProvinces().subscribe(data => {
      this.provinces = data as Province[];
    });
  }

  setProvinceCode(target: any) {
    this.provinceCode = Number(target.value);
    this.provinceSelected = this.provinces.find(p => p.code == this.provinceCode)!;
    
    this.location.getDistricts(this.provinceCode).subscribe(data => {
      this.districts = data as District[];
      this.wards = []; 
      this.postForm.patchValue({ district: 0, ward: 0 });
      this.shippingFee = 0;
      this.updateTotal();
    });
  }

  setDistrictCode(target: any) {
    this.districtCode = Number(target.value);
    this.districtSelected = this.districts.find(d => d.code == this.districtCode)!;

    this.location.getWards(this.districtCode).subscribe(data => {
      this.wards = data as Ward[];
      this.postForm.patchValue({ ward: 0 });
      this.shippingFee = 0;
      this.updateTotal();
    });
  }

  setWardCode(target: any) {
    this.wardCode = target.value;
    this.wardSelected = this.wards.find(w => w.code == this.wardCode)!;
    this.calculateShipping();
  }

  // --- LOGIC ĐẶT HÀNG ---
  checkOut() {
    if (this.postForm.invalid) {
      this.toastr.error('Vui lòng điền đầy đủ thông tin nhận hàng', 'Hệ thống');
      this.postForm.markAllAsTouched();
      return;
    }

    const fullAddress = `${this.postForm.value.number}, ${this.wardSelected.name}, ${this.districtSelected.name}, ${this.provinceSelected.name}`;

    Swal.fire({
      title: 'Xác nhận đặt hàng?',
      text: `Tổng thanh toán: ${this.totalPay.toLocaleString()}đ`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Đặt hàng'
    }).then((result) => {
      if (result.isConfirmed) {
        let email = this.sessionService.getUser();
        
        debugger;
        // Gán thông tin vào đối tượng cart trước khi gửi
        this.cart.address = fullAddress;
        this.cart.phone = this.postForm.value.phone;
        this.cart.amount = this.totalPay; // Lưu tổng tiền đã có ship
        this.cart.shippingFee = this.shippingFee; // Lưu phí ship

        // Cập nhật địa chỉ vào Cart trong DB trước (vì Backend lấy address từ Cart Entity)
        this.cart.address = fullAddress;
        this.cart.phone = this.postForm.value.phone;
        
        this.cartService.updateCart(email, this.cart).subscribe(() => {
          // Sau khi update Cart thành công mới gọi Checkout
          this.orderService.post(email, this.cart).subscribe(data => {
            let order = data as Order;
            this.sendMessage(order.ordersId);
            Swal.fire('Thành công!', 'Đơn hàng đã được tạo thành công.', 'success');
            this.router.navigate(['/profile']);
          }, () => this.toastr.error('Lỗi khi tạo đơn hàng', 'Hệ thống'));
        });
      }
    });
  }

  payWithVnpay() {
    if (this.postForm.invalid) {
      this.toastr.error("Vui lòng nhập đầy đủ thông tin", "Hệ thống");
      return;
    }
    
    let email = this.sessionService.getUser();
    const fullAddress = `${this.postForm.value.number}, ${this.wardSelected.name}, ${this.districtSelected.name}, ${this.provinceSelected.name}`;

    const payload = {
      amount: this.totalPay, 
      phone: this.postForm.value.phone,
      address: fullAddress,
      email: email,
      cartId: this.cart.cartId,
      shippingFee: this.shippingFee
    };

    this.orderService.createPaymentVnpay(payload).subscribe((res: any) => {
      if (res.paymentUrl) {
        window.location.href = res.paymentUrl;
      }
    });
  }

  sendMessage(id: number) {
    let chatMessage = new ChatMessage(this.cart.user.name, ' đã đặt một đơn hàng');
    this.notificationService.post(new Notification(0, `${this.cart.user.name} đã đặt đơn hàng #${id}`)).subscribe(() => {
      this.webSocketService.sendMessage(chatMessage);
    });
  }
}