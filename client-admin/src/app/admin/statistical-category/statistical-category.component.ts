import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Chart, registerables } from 'chart.js';
import { CategoryBestSeller } from 'src/app/common/CategoryBestSeller';
import { PageService } from 'src/app/services/page.service';
import { StatisticalService } from 'src/app/services/statistical.service';

@Component({
  selector: 'app-statistical-category',
  templateUrl: './statistical-category.component.html',
  styleUrls: ['./statistical-category.component.css']
})
export class StatisticalCategoryComponent implements OnInit, OnDestroy {

  categoryBestSeller: CategoryBestSeller[] = [];
  listData!: MatTableDataSource<CategoryBestSeller>;
  lengthCategoryBestSeller: number = 0;
  columns: string[] = ['index', 'name', 'count', 'amount'];

  labelsCategory: string[] = [];
  dataMoney: number[] = [];

  myCharPie!: Chart;

  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private statisticalService: StatisticalService, private pageService: PageService) {
    Chart.register(...registerables);
  }

  ngOnInit(): void {
    this.pageService.setPageActive('category-best-seller');
    this.getCategoryBestSeller();
  }

  getCategoryBestSeller() {
    this.statisticalService.getStatisticalBestSeller().subscribe({
      next: (data) => {
        this.categoryBestSeller = data as CategoryBestSeller[];
        this.listData = new MatTableDataSource(this.categoryBestSeller);
        
        // Timeout nhẹ để đảm bảo ViewChild đã sẵn sàng
        setTimeout(() => {
          this.listData.sort = this.sort;
          this.listData.paginator = this.paginator;
        });

        this.lengthCategoryBestSeller = this.categoryBestSeller.length;

        // Reset dữ liệu trước khi push
        this.labelsCategory = [];
        this.dataMoney = [];

        this.categoryBestSeller.forEach(item => {
          this.dataMoney.push(item.amount);
          this.labelsCategory.push(item.name);
        });

        this.loadChartPie();
      },
      error: (err) => console.error("Lỗi lấy dữ liệu danh mục:", err)
    });
  }

  loadChartPie() {
    // Xóa chart cũ nếu đã tồn tại để tránh lỗi "Canvas is already in use"
    if (this.myCharPie) {
      this.myCharPie.destroy();
    }

    const ctx = document.getElementById('charCategory') as HTMLCanvasElement;
    if (!ctx) return;

    this.myCharPie = new Chart(ctx, {
      type: 'pie',
      data: {
        labels: this.labelsCategory,
        datasets: [{
          label: 'Doanh thu theo loại hàng',
          data: this.dataMoney,
          backgroundColor: [
            '#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', 
            '#858796', '#5a5c69', '#f8f9fc', '#dddfeb'
          ],
          hoverOffset: 10,
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 20, usePointStyle: true }
          },
          tooltip: {
            callbacks: {
              label: (context: any) => {
                let label = context.label || '';
                let value = context.raw || 0;
                return ` ${label}: ${value.toLocaleString('vi-VN')} VND`;
              }
            }
          }
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.myCharPie) {
      this.myCharPie.destroy();
    }
  }
}