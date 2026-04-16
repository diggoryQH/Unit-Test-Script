import { Component, EventEmitter, OnInit, Output, TemplateRef } from '@angular/core';
import { FormControl, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { Category } from 'src/app/common/Category';
import { Product } from 'src/app/common/Product';
import { CategoryService } from 'src/app/services/category.service';
import { ProductService } from 'src/app/services/product.service';
import { UploadService } from 'src/app/services/upload.service';

@Component({
  selector: 'app-add-product',
  templateUrl: './add-product.component.html',
  styleUrls: ['./add-product.component.css']
})
export class AddProductComponent implements OnInit {

  product!: Product;
  selectFile!: File;
  url: string = 'https://res.cloudinary.com/veggie-shop/image/upload/v1633434089/products/jq4drid7ttqsxwd15xn9.jpg';
  image: string = this.url;
  postForm!: FormGroup;
  categories!: Category[];

  @Output() saveFinish: EventEmitter<any> = new EventEmitter<any>();

  constructor(
    private modalService: NgbModal, 
    private categoryService: CategoryService, 
    private productService: ProductService, 
    private toastr: ToastrService, 
    private uploadService: UploadService
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.getCategories();
  }

  initForm() {
    const today = new Date();
    const nextWeek = new Date();
    nextWeek.setDate(today.getDate() + 7); // Mặc định gợi ý hết hạn sau 7 ngày

    this.postForm = new FormGroup({
      productId: new FormControl(0),
      name: new FormControl(null, [Validators.minLength(4), Validators.required]),
      quantity: new FormControl(null, [Validators.min(1), Validators.required]),
      price: new FormControl(null, [Validators.required, Validators.min(1000)]),
      costPrice: new FormControl(null, [Validators.required, Validators.min(1000)]),
      discount: new FormControl(0, [Validators.required, Validators.min(0), Validators.max(100)]),
      description: new FormControl(null, Validators.required),
      enteredDate: new FormControl(today.toISOString().split('T')[0]),
      expiryDate: new FormControl(nextWeek.toISOString().split('T')[0], Validators.required),
      categoryId: new FormControl(1),
      status: new FormControl(true),
      sold: new FormControl(0),
      weight: new FormControl(null, [Validators.required, Validators.min(0)]),
      unit: new FormControl('kg', Validators.required),
      origin: new FormControl(null, Validators.required),
    }, { validators: this.dateLessThan('enteredDate', 'expiryDate') });
  }

  // Custom Validator kiểm tra ngày hết hạn phải sau ngày nhập
  dateLessThan(from: string, to: string) {
    return (group: AbstractControl): ValidationErrors | null => {
      const f = group.get(from)?.value;
      const t = group.get(to)?.value;
      if (f && t && new Date(t) < new Date(f)) {
        return { dateInvalid: true };
      }
      return null;
    };
  }

  save() {
    if (this.postForm.invalid) {
      this.toastr.warning('Vui lòng kiểm tra lại thông tin!', 'Hệ thống');
      return;
    }

    this.product = this.postForm.value;
    this.product.category = new Category(this.postForm.value.categoryId, '');
    this.product.image = this.image;

    this.productService.save(this.product).subscribe({
      next: (data) => {
        this.toastr.success('Thêm sản phẩm thành công!', 'Hệ thống');
        this.saveFinish.emit('done');
        this.modalService.dismissAll();
        this.resetForm();
      },
      error: (err) => {
        this.toastr.error('Có lỗi xảy ra khi lưu!', 'Hệ thống');
      }
    });
  }

  resetForm() {
    this.initForm();
    this.image = this.url;
  }

  getCategories() {
    this.categoryService.getAll().subscribe(data => {
      this.categories = data as Category[];
    });
  }

  onFileSelect(event: any) {
    this.selectFile = event.target.files[0];
    this.uploadService.uploadProduct(this.selectFile).subscribe(response => {
      if (response) {
        this.image = response.secure_url;
        this.toastr.info('Đã tải ảnh lên!', 'Hệ thống');
      }
    });
  }

  open(content: TemplateRef<any>) {
    this.modalService.open(content, { centered: true, size: 'lg', backdrop: 'static' });
  }
}