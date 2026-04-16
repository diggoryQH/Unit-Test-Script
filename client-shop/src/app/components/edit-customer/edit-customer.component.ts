import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { CustomerService } from 'src/app/services/customer.service';
import { Customer } from 'src/app/common/Customer';

@Component({
  selector: 'app-edit-customer',
  templateUrl: './edit-customer.component.html',
  styleUrls: ['./edit-customer.component.css']
})
export class EditCustomerComponent implements OnInit {
  @Input() id!: number;
  @Output() editFinish = new EventEmitter<string>();
  customer!: Customer;
  postForm!: FormGroup;
  image!: string;

  constructor(
    private customerService: CustomerService,
    private toastr: ToastrService,
    public modalService: NgbModal
  ) {}

  ngOnInit(): void {
    this.getCustomer();
  }

  getCustomer() {
    this.customerService.getOne(this.id).subscribe(data => {
      this.customer = data as Customer;
      this.postForm = new FormGroup({
        'userId': new FormControl(this.customer.userId),
        'email': new FormControl(this.customer.email, [Validators.minLength(4), Validators.email, Validators.required]),
        'name': new FormControl(this.customer.name, [Validators.minLength(4), Validators.required]),
        'password': new FormControl(this.customer.password, [Validators.minLength(6), Validators.required]),
        'address': new FormControl(this.customer.address, [Validators.minLength(4), Validators.required]),
        'phone': new FormControl(this.customer.phone, [Validators.minLength(4), Validators.required, Validators.pattern('(0)[0-9]{9}')]),
        'gender': new FormControl(this.customer.gender),
        'registerDate': new FormControl(this.customer.registerDate),
        'status': new FormControl(1),
        'token': new FormControl(this.customer.token),
      });
      this.image = this.customer.image;
    }, error => {
      this.toastr.error('Lỗi truy xuất dữ liệu! ', 'Hệ thống');
    });
  }

  onSubmit() {
    if (this.postForm.valid) {
      this.customerService.update(this.id, this.customer).subscribe(data => {
        this.toastr.success('Cập nhật thành công!', 'Hệ thống');
        this.editFinish.emit('done');
        this.modalService.dismissAll();
      }, error => {
        this.toastr.error('Lỗi cập nhật dữ liệu! ', 'Hệ thống');
      });
    } else {
      this.toastr.error('Hãy kiểm tra lại dữ liệu! ', 'Hệ thống');
    }
    
  }
}