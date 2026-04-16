package com.nongsan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nongsan.service.GHNService;

@CrossOrigin("*")
@RestController
@RequestMapping("api/shipping")
public class ShippingApi {

    @Autowired
    GHNService ghnService;

    @GetMapping("/fee")
    public ResponseEntity<Double> getShippingFee(
            @RequestParam("wardCode") String wardCode,
            @RequestParam("districtId") Integer districtId,
            @RequestParam("weight") Integer weight) {
        
        Double fee = ghnService.calculateFee(wardCode, districtId, weight);
        return ResponseEntity.ok(fee);
    }
}
