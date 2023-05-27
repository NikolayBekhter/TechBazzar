package ru.bazzar.core.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bazzar.core.entities.OrderItem;
import ru.bazzar.core.repositories.OrderItemRepository;
import ru.bazzar.core.services.interf.OrderItemService;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderItemServiceImpl extends AbstractService<OrderItem, Long> implements OrderItemService {
    private final OrderItemRepository orderItemRepository;

    @Override
    JpaRepository<OrderItem, Long> getRepository() {
        return orderItemRepository;
    }

    public OrderItem saveAndReturnOrderItem(OrderItem orderItem) {
        return getRepository().save(orderItem);
    }
}
