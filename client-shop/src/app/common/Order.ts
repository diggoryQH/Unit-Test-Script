import { Customer } from "./Customer";

export class Order {
    ordersId!: number;
    orderDate!: Date;
    amount!: number;
    address!: string;
    phone!: string;
    shippingFee!: number; // Thêm để khớp backend
    weight!: number;      // Thêm để khớp backend
    status!: number;      // 0: Chờ, 1: Giao, 2: Hoàn tất, 3: Hủy, 4: Chờ duyệt Hủy/Trả, 6: Đã trả hàng
    user!: Customer;
}