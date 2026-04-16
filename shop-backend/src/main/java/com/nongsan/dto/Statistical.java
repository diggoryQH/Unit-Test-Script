package com.nongsan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistical {
    private int month;
    private Double amount; // Doanh thu
    private Double profit; // Lợi nhuận (Mới thêm)
}