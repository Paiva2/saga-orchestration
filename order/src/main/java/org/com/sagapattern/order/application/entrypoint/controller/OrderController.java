package org.com.sagapattern.order.application.entrypoint.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.sagapattern.order.domain.usecase.createOrder.CreateOrderUsecase;
import org.com.sagapattern.order.domain.usecase.createOrder.dto.CreateOrderInput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final CreateOrderUsecase createOrderUsecase;

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody @Valid CreateOrderInput input) {
        createOrderUsecase.execute(input);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
