package com.nongsan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nongsan.entity.Order;
import com.nongsan.entity.OrderReturn;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {

  OrderReturn findByOrder(Order order);

}
