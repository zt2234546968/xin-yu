package com.xinyu.ecommerce.controller;

import com.xinyu.ecommerce.common.Result;
import com.xinyu.ecommerce.entity.Ceping;
import com.xinyu.ecommerce.entity.OrderList;
import com.xinyu.ecommerce.entity.dto.CreateOrderListRequest;
import com.xinyu.ecommerce.repository.CepingRepository;
import com.xinyu.ecommerce.service.OrderListService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orderList")
public class OrderListController {

    @Autowired
    private OrderListService orderListService;

    @Autowired
    private CepingRepository cepingRepository;

    @PostMapping
    @Operation(summary = "创建订单", description = "创建新的订单，关联测评ID、订单号和订单截图")
    public Result<OrderList> create(@RequestBody CreateOrderListRequest request) {
        try {
            System.out.println("========== OrderListController.create() ==========");
            System.out.println("接收到的请求DTO: " + request);
            System.out.println("cepingId: " + request.getCepingId());
            System.out.println("orderNumber: " + request.getOrderNumber());
            System.out.println("orderScreenshot: " + (request.getOrderScreenshot() != null ? "有数据" : "null"));

            OrderList orderList = new OrderList();
            orderList.setCepingId(request.getCepingId());
            orderList.setOrderNumber(request.getOrderNumber());
            orderList.setOrderScreenshot(request.getOrderScreenshot());

            if (request.getCepingId() != null && !request.getCepingId().isEmpty()) {
                System.out.println("开始查找ceping: " + request.getCepingId());
                Ceping ceping = cepingRepository.findById(request.getCepingId()).orElse(null);
                System.out.println("查找到的ceping: " + ceping);
                if (ceping != null) {
                    orderList.setCeping(ceping);
                    System.out.println("关联成功！");
                }
            }

            OrderList created = orderListService.create(orderList);
            System.out.println("保存后的created: " + created);
            System.out.println("保存后的created.getCeping(): " + created.getCeping());
            System.out.println("================================================");
            return Result.success("创建成功", created);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有订单", description = "获取所有订单列表，过滤已删除的订单")
    public Result<List<OrderList>> list() {
        try {
            List<OrderList> orderLists = orderListService.getAll();
            return Result.success(orderLists);
        } catch (Exception e) {
            return Result.error(500, "获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    public Result<OrderList> getById(@PathVariable String id) {
        try {
            OrderList orderList = orderListService.getById(id);
            if (orderList != null) {
                return Result.success(orderList);
            } else {
                return Result.error(404, "订单列表不存在");
            }
        } catch (Exception e) {
            return Result.error(500, "获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/ceping/{cepingId}")
    @Operation(summary = "根据测评ID获取订单", description = "根据测评ID获取该测评下的所有订单")
    public Result<List<OrderList>> getByCepingId(@PathVariable String cepingId) {
        try {
            List<OrderList> orderLists = orderListService.getByCepingId(cepingId);
            return Result.success(orderLists);
        } catch (Exception e) {
            return Result.error(500, "获取失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新订单", description = "根据订单ID更新订单信息，包括本金、汇率、佣金、PP价格等")
    public Result<OrderList> update(@PathVariable String id, @RequestBody OrderList orderList) {
        try {
            System.out.println("========== OrderListController.update() ==========");
            System.out.println("接收到的ID: " + id);
            System.out.println("接收到的OrderList: " + orderList);
            System.out.println("principal值: " + orderList.getPrincipal());
            System.out.println("exchangeRate值: " + orderList.getExchangeRate());
            System.out.println("commission值: " + orderList.getCommission());
            System.out.println("ppPrice值: " + orderList.getPpPrice());
            System.out.println("================================================");
            orderList.setId(id);
            OrderList updated = orderListService.update(orderList);
            if (updated != null) {
                return Result.success("更新成功", updated);
            } else {
                return Result.error(404, "订单列表不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "根据订单ID删除订单（软删除）")
    public Result<Void> delete(@PathVariable String id) {
        try {
            orderListService.delete(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }
}