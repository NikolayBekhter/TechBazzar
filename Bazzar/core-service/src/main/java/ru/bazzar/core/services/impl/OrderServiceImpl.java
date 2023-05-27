package ru.bazzar.core.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bazzar.core.api.*;
import ru.bazzar.core.entities.Order;
import ru.bazzar.core.entities.OrderItem;
import ru.bazzar.core.entities.Product;
import ru.bazzar.core.integrations.CartServiceIntegration;
import ru.bazzar.core.integrations.OrganizationServiceIntegration;
import ru.bazzar.core.integrations.UserServiceIntegration;
import ru.bazzar.core.repositories.OrderRepository;
import ru.bazzar.core.services.interf.OrderService;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl extends AbstractService<Order, Long> implements OrderService {
    private final ProductServiceImpl productServiceImpl;
    private final OrderItemServiceImpl orderItemServiceImpl;
    private final OrderRepository orderRepository;
    private final CartServiceIntegration cartService;
    private final UserServiceIntegration userService;
    private final PurchaseHistoryServiceImpl historyService;
    private final OrganizationServiceIntegration organizationService;

    @Override
    JpaRepository<Order, Long> getRepository() {
        return orderRepository;
    }

    @Transactional
    public Order create(String username) {
        CartDto cartDto = cartService.getCurrentCart(username);
        Order order = new Order();
        order.setUsername(username);
        order.setTotalPrice(cartDto.getTotalPrice());
        getRepository().save(order);

        List<OrderItem> orderItems = cartDto.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(productServiceImpl.findById(cartItem.getProductId()));
                    orderItem.setOrder(order);
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setPricePerProduct(cartItem.getPricePerProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    return orderItemServiceImpl.saveAndReturnOrderItem(orderItem);
                }).toList();
        return order;

    }

    public List<Order> getOrder(String username) {
        return orderRepository.findAllByUsername(username);
    }

    @Transactional
    public void payment(String username, Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()->new ResourceNotFoundException("Ордер с id: " + orderId +" не найден!"));
        if (!order.isStatus()) {
            List<UserDto> listUserDto = new ArrayList<>();
            List<Product> listProduct = new ArrayList<>();
            List<PurchaseHistoryDto> historyDtoList = new ArrayList<>();

            for (OrderItem orderItem : order.getItems().stream().toList()) {
                OrganizationDto organizationDto = organizationService.getOrganizationByTitle(orderItem.getProduct().getOrganizationTitle());
                UserDto userDto = new UserDto();
                PurchaseHistoryDto historyDto = new PurchaseHistoryDto();
                Product product = new Product();
                product.setId(orderItem.getProduct().getId());
                product.setQuantity(orderItem.getQuantity());
                userDto.setEmail(organizationDto.getOwner());
                userDto.setBalance(orderItem.getPrice());
                historyDto.setEmail(username);
                historyDto.setProductTitle(orderItem.getProduct().getTitle());
                historyDto.setOrganizationTitle(organizationDto.getTitle());
                historyDto.setQuantity(orderItem.getQuantity());
                listUserDto.add(userDto);
                listProduct.add(product);
                historyDtoList.add(historyDto);
            }
            for (Product product : listProduct) {
                productServiceImpl.changeQuantity(product);
            }
            userService.payment(username, order.getTotalPrice());
            for (PurchaseHistoryDto historyDto : historyDtoList) {
                historyService.saveDto(historyDto);
            }
            for (UserDto userDto : listUserDto) {
                userService.receivingProfit(userDto);
            }
            order.setStatus(true);
        }
        getRepository().save(order);
    }

    public boolean isRefundOrder(Long id) {
        Order order = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ с id: " + id + " не найден!"));
        Duration d = Duration.between(order.getUpdatedAt(), LocalDateTime.now());
        return d.toSeconds() <= 86400;
    }
    //проведение оплаты
    public void refundPayment(String username, Long orderId) {
        Order order = getRepository().findById(orderId)
                .orElseThrow(()->new ResourceNotFoundException("Ордер с id: " + orderId +" не найден!"));
        if (order.isStatus()) {
            List<UserDto> listUserDto = new ArrayList<>();
            List<Product> listProduct = new ArrayList<>();

            for (OrderItem orderItem : order.getItems().stream().toList()) {
                OrganizationDto organizationDto = organizationService.getOrganizationByTitle(orderItem.getProduct().getOrganizationTitle());
                UserDto userDto = new UserDto();
                Product product = new Product();
                product.setId(orderItem.getProduct().getId());
                product.setQuantity(-orderItem.getQuantity());
                userDto.setEmail(organizationDto.getOwner());
                userDto.setBalance(orderItem.getPrice());
                listUserDto.add(userDto);
                listProduct.add(product);
            }
            for (Product product : listProduct) {
                productServiceImpl.changeQuantity(product);
            }
            userService.refundPayment(username, order.getTotalPrice());

            for (UserDto userDto : listUserDto) {
                userService.refundProfit(userDto);
            }
            order.setStatus(false);
        }
        getRepository().save(order);
    }
}
