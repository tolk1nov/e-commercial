package com.example.ecommercial.service.order;

import com.example.ecommercial.dao.BasketDao;
import com.example.ecommercial.dao.OrderDao;
import com.example.ecommercial.dao.ProductDao;
import com.example.ecommercial.dao.UserDao;
import com.example.ecommercial.controller.dto.response.BaseResponse;
import com.example.ecommercial.controller.dto.response.BasketGetResponse;
import com.example.ecommercial.controller.dto.response.OrderGetResponse;
import com.example.ecommercial.controller.dto.response.UserOrdersGetResponse;
import com.example.ecommercial.domain.entity.*;
import com.example.ecommercial.domain.enums.OrderStatus;
import com.example.ecommercial.domain.enums.UserRole;
import com.example.ecommercial.service.history.HistoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderDao orderDao;
    private final ModelMapper modelMapper;
    private final BasketDao basketDao;
    private final UserDao userDao;
    private final ProductDao productDao;
    private final HistoryService historyService;


    public BaseResponse delete(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        UserEntity user = order.getUsers();
        ProductEntity product = order.getProducts();

        user.setBalance(user.getBalance() + order.getTotalPrice());
        product.setAmount(product.getAmount() + order.getAmount());

        orderDao.deleteById(id);
        userDao.save(user);
        productDao.save(product);
        BaseResponse<List<UserOrdersGetResponse>> responce = getALl(0);
        responce.setMessage("deleted");

        return responce;
    }


    public BaseResponse<List<UserOrdersGetResponse>> getALl(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 5);
        Page<UserEntity> userEntityPage = userDao
                .findUserEntitiesByOrderEntitiesIsNotEmptyAndChatIdNotNull(pageable);
        int totalPages = userEntityPage.getTotalPages();
        List<UserEntity> users = userEntityPage.getContent();
        if (users.isEmpty()) {
            return BaseResponse.<List<UserOrdersGetResponse>>builder()
                    .totalPageAmount(0)
                    .status(404)
                    .data(Collections.emptyList())
                    .build();
        }
        List<UserOrdersGetResponse> allUserOrders = new LinkedList<>();
        for (UserEntity user : users) {
            double totalSum = 0;
            List<OrderEntity> orderEntities = orderDao
                    .findOrderEntitiesByUsersId(user.getId()).get();
            for (OrderEntity orderEntity : orderEntities) {
                totalSum += orderEntity.getTotalPrice();
            }
            allUserOrders.add(UserOrdersGetResponse.builder()
                    .username(user.getName())
                    .totalSum(totalSum)
                    .orders(modelMapper
                            .map(orderEntities, new TypeToken<List<OrderGetResponse>>() {
                            }
                                    .getType()))
                    .build());
        }
        return BaseResponse.<List<UserOrdersGetResponse>>builder()
                .totalPageAmount((totalPages==0)?0:totalPages-1)
                .message("Success")
                .data(allUserOrders)
                .build();
    }

    public BaseResponse<BasketGetResponse> orderProduct(Long basketId) {
        BasketEntity basket = basketDao.findById(basketId).get();
        UserEntity user = basket.getUsers();
        ProductEntity product = basket.getProducts();

        int status;
        String message;

        double balance = user.getBalance();
        int basketAmount = basket.getProductAmount();
        double price = product.getPrice();
        int productAmount = product.getAmount();

        double totalPrice = price * basketAmount;
        BasketGetResponse basketGetResponse = modelMapper
                .map(basket, BasketGetResponse.class);
        if (basketAmount > productAmount) {
            status = 401;
            message = "Your ordered amount is greater than total product amount";
        } else if (totalPrice > balance) {
            status = 402;
            message = "You do not have enough balance";
        } else {
            OrderEntity order = OrderEntity.builder()
                    .amount(basketAmount)
                    .products(product)
                    .totalPrice(totalPrice)
                    .users(user)
                    .orderStatus(OrderStatus.NEW)
                    .build();

            user.setBalance(user.getBalance() - totalPrice);
            product.setAmount(product.getAmount() - basketAmount);

            orderDao.save(order);
            userDao.save(user);
            productDao.save(product);
            basketDao.deleteById(basket.getId());
            status = 200;
            message = "Product has been ordered";
        }
        return BaseResponse.<BasketGetResponse>builder()
                .status(status)
                .message(message)
                .data(basketGetResponse)
                .build();
    }

    public BaseResponse<List<OrderGetResponse>> findUserOrders(Long chatId) {
        UserEntity userEntity = userDao.findUserEntitiesByChatId(chatId).get();
        List<OrderEntity> orders = orderDao
                .findOrderEntitiesByUsersId(userEntity.getId()).get();
        if (orders.isEmpty()) {
            return BaseResponse.<List<OrderGetResponse>>builder()
                    .status(404)
                    .build();
        }
        return BaseResponse.<List<OrderGetResponse>>builder()
                .status(200)
                .data(modelMapper
                        .map(orders, new TypeToken<List<OrderGetResponse>>() {
                        }.getType()))
                .build();
    }

    public BaseResponse<List<UserOrdersGetResponse>> changeStatus(Long orderId, String status) {
        String message;
        if (status.equals("CANCEL")) {
            message = "Order has been cancelled";
            delete(orderId);
        } else {
            message = "Product has been ordered";
            OrderEntity orderEntity = orderDao.findById(orderId).get();
            HistoryEntity history = HistoryEntity.builder()
                    .amount(orderEntity.getAmount())
                    .totalPrice(orderEntity.getTotalPrice())
                    .name(orderEntity.getProducts().getName())
                    .description(orderEntity.getProducts().getDescription())
                    .users(orderEntity.getUsers())
                    .categoryName(orderEntity.getProducts().getCategories().getName())
                    .build();
            historyService.save(history);
            orderDao.deleteById(orderId);
        }
        BaseResponse<List<UserOrdersGetResponse>> response = getALl(0);
        response.setMessage(message);
        return response;
    }
}
