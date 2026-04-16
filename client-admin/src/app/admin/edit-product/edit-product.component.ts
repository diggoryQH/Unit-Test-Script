import { Component, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core';
import { FormControl, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { Category } from 'src/app/common/Category';
import { Product } from 'src/app/common/Product';
import { CategoryService } from 'src/app/services/category.service';
import { ProductService } from 'src/app/services/product.service';
import { UploadService } from 'src/app/services/upload.service';

@Component({
  selector: 'app-edit-product',
  templateUrl: './edit-product.component.html',
  styleUrls: ['./edit-product.component.css']
})
export class EditProductComponent implements OnInit {

  product!: Product;
  selectFile!: File;
  image: string = '';
  postForm!: FormGroup;
  categories!: Category[];

  @Input() id!: number;
  @Output() editFinish: EventEmitter<any> = new EventEmitter<any>();

  constructor(
    private modalService: NgbModal,
    private categoryService: CategoryService,
    private productService: ProductService,
    private toastr: ToastrService,
    private uploadService: UploadService
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.getCategories();
  }

  initForm() {
    this.postForm = new FormGroup({
      productId: new FormControl(0),
      name: new FormControl(null, [Validators.minLength(4), Validators.required]),
      quantity: new FormControl(null, [Validators.min(0), Validators.required]),
      price: new FormControl(null, [Validators.required, Validators.min(1000)]),
      costPrice: new FormControl(null, [Validators.required, Validators.min(1000)]),
      discount: new FormControl(null, [Validators.required, Validators.min(0), Validators.max(100)]),
      description: new FormControl(null, Validators.required),
      enteredDate: new FormControl(null, Validators.required),
      expiryDate: new FormControl(null, Validators.required),
      categoryId: new FormControl(null, Validators.required),
      status: new FormControl(true),
      sold: new FormControl(0),
      weight: new FormControl(null, [Validators.required, Validators.min(0)]),
      unit: new FormControl(null, Validators.required),
      origin: new FormControl(null, Validators.required),
    }, { validators: this.dateValidator('enteredDate', 'expiryDate') });
  }

  // So sánh ngày nhập và ngày hết hạn
  dateValidator(from: string, to: string) {
    return (group: AbstractControl): ValidationErrors | null => {
      const f = group.get(from)?.value;
      const t = group.get(to)?.value;
      if (f && t && new Date(t) < new Date(f)) return { dateInvalid: true };
      return null;
    };
  }

  getProduct() {
    this.productService.getOne(this.id).subscribe({
      next: (data) => {
        this.product = data as Product;
        this.postForm.patchValue({
          ...this.product,
          categoryId: this.product.category.categoryId
        });
        this.image = this.product.image;
      },
      error: () => this.toastr.error('Không tìm thấy sản phẩm!', 'Lỗi')
    });
  }

  update() {
    if (this.postForm.invalid) {
      this.toastr.warning('Vui lòng kiểm tra lại thông tin!', 'Cảnh báo');
      return;
    }

    const updateData = {
      ...this.postForm.value,
      image: this.image,
      category: new Category(this.postForm.value.categoryId, '')
    };

    this.productService.update(updateData, this.id).subscribe({
      next: () => {
        this.toastr.success('Cập nhật thành công!', 'Hệ thống');
        this.editFinish.emit('done');
        this.modalService.dismissAll();
      },
      error: () => this.toastr.error('Có lỗi xảy ra!', 'Hệ thống')
    });
  }

  getCategories() {
    this.categoryService.getAll().subscribe(data => {
      this.categories = data as Category[];
    });
  }

  onFileSelect(event: any) {
    this.selectFile = event.target.files[0];
    this.uploadService.uploadProduct(this.selectFile).subscribe(res => {
      if (res) {
        this.image = res.secure_url;
        this.toastr.info('Đã cập nhật ảnh tạm thời!', 'Hệ thống');
      }
    });
  }

  open(content: TemplateRef<any>) {
    this.getProduct(); // Load lại dữ liệu mỗi khi mở modal
    this.modalService.open(content, { centered: true, size: 'lg', backdrop: 'static' });
  }
}