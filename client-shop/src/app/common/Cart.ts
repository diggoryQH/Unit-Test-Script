import { Customer } from "./Customer";

export class Cart {
    'cartId': number;
    'phone': string;
    'address': string;
    'amount': number;
    'shippingFee': number;
    'user': Customer;

    constructor(id:number) {
        this.cartId = id;
    }
}
