package com.nongsan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nongsan.entity.Order;
import com.nongsan.entity.OrderReturn;
import com.nongsan.repository.OrderRepository;
import com.nongsan.repository.OrderReturnRepository;

@RestController
@RequestMapping("api/returns")
@CrossOrigin("*")
public class OrderReturnApi {
    @Autowired OrderReturnRepository returnRepo;
    @Autowired OrderRepository orderRepo;

    @PostMapping("/request")
    public ResponseEntity<OrderReturn> createRequest(@RequestBody OrderReturn req) {
        Order order = orderRepo.findById(req.getOrder().getOrdersId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        // Lưu yêu cầu trả hàng/hủy hàng
        req.setStatus(0); // 0: Chờ admin duyệt
        OrderReturn savedReturn = returnRepo.save(req);

        // Chuyển trạng thái Order sang "Đang chờ duyệt" (Status 4)
        order.setStatus(4); 
        orderRepo.save(order);

        return ResponseEntity.ok(savedReturn);
    }

    // LẤY THÔNG TIN ĐỂ HIỂN THỊ TRÊN POPUP ADMIN
    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderReturn> getByOrderId(@PathVariable Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        OrderReturn ret = returnRepo.findByOrder(order); // Cần định nghĩa trong Repository
        return (ret != null) ? ResponseEntity.ok(ret) : ResponseEntity.notFound().build();
    }

    @PutMapping("/admin/process/{id}")
    public ResponseEntity<OrderReturn> processReturn(@PathVariable Long id, @RequestParam int action, @RequestParam String note) {
        OrderReturn ret = returnRepo.findById(id).get();
        Order order = ret.getOrder();
        
        if (action == 1) { // CHẤP NHẬN
            ret.setStatus(1);
            // Nếu order đang ở trạng thái 0,1 (chưa hoàn tất) thì là HỦY
            // Nếu order đang ở trạng thái 2 (đã hoàn tất) thì là TRẢ HÀNG
            if (order.getStatus() == 4) {
                // Tùy logic: nếu trước đó status là 2 thì thành 6 (trả hàng), nếu là 0,1 thì thành 3 (hủy)
                order.setStatus(ret.getRefundAmount() != null ? 6 : 3); 
            }
        } else { // TỪ CHỐI
            ret.setStatus(2);
            // Trả lại trạng thái cũ dựa trên logic kinh doanh (ví dụ quay lại trạng thái Giao hàng hoặc Hoàn tất)
            order.setStatus(2); 
        }
        ret.setAdminNote(note);
        orderRepo.save(order);
        return ResponseEntity.ok(returnRepo.save(ret));
    }
}