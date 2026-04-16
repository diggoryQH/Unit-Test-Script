import { Category } from "./Category";

export class Product {
    productId: number;
    name!: string;
    quantity!: number;
    price!: number;
    discount!: number;
    image!: string;
    description!: string;
    enteredDate!: Date;
    category!: Category;
    status!: boolean;
    sold!: number;
    // --- Các trường mới ---
    costPrice?: number; 
    weight!: number;
    unit!: string;
    origin!: string;

    constructor(id: number) {
        this.productId = id;
    }
}