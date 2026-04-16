import { Component, OnDestroy, OnInit } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { CustomerService } from 'src/app/services/customer.service';
import { OrderService } from 'src/app/services/order.service';
import { StatisticalService } from 'src/app/services/statistical.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  // Biến đếm tổng quát
  orderHandle: number = 0;
  customerLength: number = 0;
  
  // Dữ liệu báo cáo
  year: number = new Date().getFullYear();
  countYears: number[] = [];
  categoryBestSellers: any[] = [];
  advancedReport: any = {
    totalRevenue: 0,
    grossProfit: 0,
    orderStats: { success: 0, canceled: 0, returned: 0 }
  };

  // Biểu đồ
  myChartMixed: any; // Biểu đồ kết hợp Doanh thu & Lợi nhuận
  myChartPie: any;

  constructor(
    private statisticalService: StatisticalService,
    private orderService: OrderService,
    private customerService: CustomerService
  ) { 
    Chart.register(...registerables); 
  }

  ngOnInit(): void {
    this.loadGeneralStats();
    this.loadYearlyData();
    this.getCountYear();
  }

  // Tải các số liệu đếm (Khách hàng, đơn chờ)
  loadGeneralStats() {
    this.customerService.getAll().subscribe((data: any) => {
      this.customerLength = data.length;
    });

    this.orderService.get().subscribe((data: any) => {
      this.orderHandle = data.filter((o: any) => o.status === 0).length;
    });
  }

  // Tải dữ liệu theo năm đã chọn
  loadYearlyData() {
    // 1. Lấy dữ liệu biểu đồ hỗn hợp (Doanh thu - Bar & Lợi nhuận - Line)
    this.statisticalService.getByMothOfYear(this.year).subscribe((data: any) => {
      // Sắp xếp tháng từ 1 đến 12
      const sortedData = data.sort((a: any, b: any) => a.month - b.month);
      
      const labels = sortedData.map((item: any) => 'Tháng ' + item.month);
      const revenueValues = sortedData.map((item: any) => item.amount);
      const profitValues = sortedData.map((item: any) => item.profit);

      this.initMixedChart(labels, revenueValues, profitValues);
    });

    // 2. Lấy báo cáo tài chính (Tổng quan) & biểu đồ tròn đơn hàng
    this.statisticalService.getAdvancedReport(this.year).subscribe((data: any) => {
      this.advancedReport = data;
      this.initPieChart();
    });

    // 3. Lấy danh mục bán chạy nhất
    this.getTopCategories();
  }

  getTopCategories() {
    this.statisticalService.getStatisticalBestSeller().subscribe({
      next: (res: any) => {
        this.categoryBestSellers = res;
      },
      error: (err) => console.error("Lỗi danh mục:", err)
    });
  }

  getCountYear() {
    this.statisticalService.getCountYear().subscribe((data: any) => {
      this.countYears = data;
    });
  }

  // Khởi tạo biểu đồ hỗn hợp: Cột (Doanh thu) và Đường (Lợi nhuận)
  initMixedChart(labels: string[], revenue: number[], profit: number[]) {
    if (this.myChartMixed) this.myChartMixed.destroy();

    this.myChartMixed = new Chart('chartBar', {
      type: 'bar', // THÊM DÒNG NÀY: Xác định kiểu biểu đồ chính
      data: {
        labels: labels,
        datasets: [
          {
            type: 'line', // Giữ nguyên type riêng cho lợi nhuận
            label: 'Lợi nhuận',
            data: profit,
            borderColor: '#e74a3b',
            backgroundColor: 'rgba(231, 74, 59, 0.1)',
            fill: true,
            tension: 0.4,
            borderWidth: 3,
            pointRadius: 4,
            order: 1 
          },
          {
            type: 'bar', // Giữ nguyên type riêng cho doanh thu
            label: 'Doanh thu',
            data: revenue,
            backgroundColor: '#4e73df',
            borderRadius: 5,
            order: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (value) => value.toLocaleString('vi-VN') + ' ₫'
            }
          }
        },
        plugins: {
          tooltip: {
            callbacks: {
              label: (context: any) => {
                return `${context.dataset.label}: ${context.raw.toLocaleString('vi-VN')} ₫`;
              }
            }
          }
        }
      }
    });
  }

  initPieChart() {
    if (this.myChartPie) this.myChartPie.destroy();
    const stats = this.advancedReport.orderStats;
    this.myChartPie = new Chart('chartPie', {
      type: 'doughnut',
      data: {
        labels: ['Thành công', 'Đã hủy', 'Trả hàng'],
        datasets: [{
          data: [stats.success, stats.canceled, stats.returned],
          backgroundColor: ['#1cc88a', '#e74a3b', '#f6c23e'],
          hoverOffset: 10
        }]
      },
      options: { 
        responsive: true, 
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' }
        }
      }
    });
  }

  setYear(newYear: any) {
    this.year = Number(newYear);
    this.loadYearlyData();
  }

  ngOnDestroy(): void {
    if (this.myChartMixed) this.myChartMixed.destroy();
    if (this.myChartPie) this.myChartPie.destroy();
  }
}