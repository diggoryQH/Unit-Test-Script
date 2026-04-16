import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ToastrService } from 'ngx-toastr';
import { Product } from 'src/app/common/Product';
import { PageService } from 'src/app/services/page.service';
import { ProductService } from 'src/app/services/product.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.css']
})
export class ProductComponent implements OnInit {

  listData!: MatTableDataSource<Product>;
  products!: Product[];
  productsLength!: number;
  // Cột hiển thị bao gồm cả Status và Expiry logic
  columns: string[] = ['image', 'productId', 'name', 'price', 'category', 'status', 'enteredDate', 'expiryDate', 'inventoryStatus', 'view', 'delete'];

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private pageService: PageService, private productService: ProductService, private toastr: ToastrService) { }

  ngOnInit(): void {
    this.pageService.setPageActive('product');
    this.getAll();
  }

  getAll() {
    this.productService.getAll().subscribe(data => {
      this.products = data as Product[];
      this.listData = new MatTableDataSource(this.products);
      this.listData.sort = this.sort;
      this.listData.paginator = this.paginator;
    }, error => {
      this.toastr.error('Lỗi kết nối máy chủ!', 'Hệ thống');
    })
  }

  // Logic phân loại tình trạng hàng dựa trên ngày hết hạn
  getExpiryStatus(expiryDate: any): string {
    if (!expiryDate) return 'none';
    const now = new Date();
    const exp = new Date(expiryDate);
    
    // Reset giờ về 0 để so sánh ngày chính xác
    now.setHours(0, 0, 0, 0);
    exp.setHours(0, 0, 0, 0);

    const diffTime = exp.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays < 0) return 'expired';      // Đã quá hạn
    if (diffDays <= 3) return 'warning';    // Sắp hết hạn (3 ngày)
    return 'good';                          // An toàn
  }

  delete(id: number, name: string) {
    Swal.fire({
      title: 'Xoá sản phẩm?',
      text: `Bạn có chắc chắn muốn xoá ${name}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#e74c3c',
      cancelButtonColor: '#95a5a6',
      confirmButtonText: 'Đồng ý',
      cancelButtonText: 'Huỷ'
    }).then((result) => {
      if (result.isConfirmed) {
        this.productService.delete(id).subscribe(() => {
          this.getAll();
          this.toastr.success('Xoá thành công!', 'Hệ thống');
        }, () => {
          this.toastr.error('Xoá thất bại!', 'Hệ thống');
        })
      }
    })
  }

  search(event: any) {
    const fValue = (event.target as HTMLInputElement).value;
    this.listData.filter = fValue.trim().toLowerCase();
  }

  finish() {
    this.getAll();
  }
}