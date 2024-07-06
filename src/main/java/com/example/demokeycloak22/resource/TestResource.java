package com.example.demokeycloak22.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class TestResource {
    @GetMapping("/customer")
    CustomerData test(){
        return new CustomerData("Phạm Ngọc Hoài", 39);
    }

    @PostMapping("/customer")
    CustomerData updateSth(@RequestBody CustomerData data) {
        if (data.getAge() > 100)
            throw new IllegalStateException("Tuổi không lớn hơn 100");

        return data;
    }
}
