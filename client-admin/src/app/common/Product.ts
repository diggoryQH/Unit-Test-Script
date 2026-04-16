import { Category } from "./Category";

export class Product {
    productId!: number;
    name!: string;
    quantity!: number;
    price!: number;
    costPrice!: number; // MỚI
    discount!: number;
    image!: string;
    description!: string;
    enteredDate!: Date;
    expiryDate!: Date; // THÊM MỚI
    category!: Category;
    status!: boolean;
    sold!: number;
    weight!: number;    // MỚI
    unit!: string;      // MỚI
    origin!: string;    // MỚI
}