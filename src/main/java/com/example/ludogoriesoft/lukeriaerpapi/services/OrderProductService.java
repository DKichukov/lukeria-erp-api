package com.example.ludogoriesoft.lukeriaerpapi.services;

import com.example.ludogoriesoft.lukeriaerpapi.dtos.ClientDTO;
import com.example.ludogoriesoft.lukeriaerpapi.dtos.OrderDTO;
import com.example.ludogoriesoft.lukeriaerpapi.dtos.OrderProductDTO;
import com.example.ludogoriesoft.lukeriaerpapi.dtos.OrderWithProductsDTO;
import com.example.ludogoriesoft.lukeriaerpapi.models.*;
import com.example.ludogoriesoft.lukeriaerpapi.repository.*;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderProductService {

    private final InvoiceOrderProductRepository invoiceOrderProductRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;
    private final PackageRepository packageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final EmailContentBuilder emailContentBuilder;
    private final EmailService emailService;

    public List<OrderProductDTO> getAllOrderProducts() {
        List<OrderProduct> orderProducts = orderProductRepository.findByDeletedFalse();
        return orderProducts.stream()
                .map(orderProduct -> modelMapper.map(orderProduct, OrderProductDTO.class))
                .toList();
    }

    public OrderProductDTO getOrderProductById(Long id) throws ChangeSetPersister.NotFoundException {
        OrderProduct order = orderProductRepository.findByIdAndDeletedFalse(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        return modelMapper.map(order, OrderProductDTO.class);
    }

    void validateOrderProductDTO(OrderProductDTO orderDTO) {
        if (orderDTO.getOrderId() != null) {
            boolean orderExists = orderRepository.existsById(orderDTO.getOrderId());
            if (!orderExists) {
                throw new ValidationException("Order does not exist with ID: " + orderDTO.getOrderId());
            }
        } else {
            throw new ValidationException("Order ID cannot be null!");
        }
        if (orderDTO.getPackageId() != null) {
            boolean orderExists = packageRepository.existsById(orderDTO.getPackageId());
            if (!orderExists) {
                throw new ValidationException("Package does not exist with ID: " + orderDTO.getPackageId());
            }
        } else {
            throw new ValidationException("Package ID cannot be null!");
        }
    }

    public OrderProductDTO createOrderProduct(OrderProductDTO orderDTO) {
        validateOrderProductDTO(orderDTO);
        OrderProduct order = orderProductRepository.save(modelMapper.map(orderDTO, OrderProduct.class));
        return modelMapper.map(order, OrderProductDTO.class);
    }

    public OrderProductDTO updateOrderProduct(Long id, OrderProductDTO orderDTO) throws ChangeSetPersister.NotFoundException {
        validateOrderProductDTO(orderDTO);

        OrderProduct existingOrderProduct = orderProductRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        OrderProduct updatedOrderProduct = modelMapper.map(orderDTO, OrderProduct.class);
        updatedOrderProduct.setId(existingOrderProduct.getId());
        orderProductRepository.save(updatedOrderProduct);
        return modelMapper.map(updatedOrderProduct, OrderProductDTO.class);
    }

    public void deleteOrderProduct(Long id) throws ChangeSetPersister.NotFoundException {
        OrderProduct order = orderProductRepository.findByIdAndDeletedFalse(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        order.setDeleted(true);
        orderProductRepository.save(order);
    }

    public List<InvoiceOrderProduct> findInvoiceOrderProductsByInvoiceId(Long invoiceId) {
        List<InvoiceOrderProduct> invoiceOrderProductsList = invoiceOrderProductRepository.findAll();

        return invoiceOrderProductsList.stream()
                .filter(orderProduct -> orderProduct.getInvoiceId() != null && orderProduct.getInvoiceId().getId().equals(invoiceId))
                .toList();
    }

    public boolean reduceProducts(List<InvoiceOrderProduct> invoiceOrderProductsList) {
        List<Product> productList = new ArrayList<>();
        for (InvoiceOrderProduct invoiceOrderProduct : invoiceOrderProductsList) {
            Optional<Product> productForReduce = productRepository.findByIdAndDeletedFalse(invoiceOrderProduct.getOrderProductId().getPackageId().getId());
            int sellingProductForReduce = invoiceOrderProduct.getOrderProductId().getNumber();
            if (productForReduce.isPresent()) {
                Product product = productForReduce.get();
                product.setAvailableQuantity(product.getAvailableQuantity() - sellingProductForReduce);
                Product savedProduct=productRepository.save(product);
                productList.add(savedProduct);
            } else return false;
        }
        return sendMailForQuantities(productList);
    }

    private boolean sendMailForQuantities(List<Product> productList) {
        List<String> emailList = userRepository.findEmailsByRoleNotCustomer();
        String body = emailContentBuilder.generateStockReportEmail(productList);
        emailService.sendHtmlEmailWithProductReport(emailList, "Доклад за наличност на продукти след изпращане на заявка", body);
        return true;
    }
    public List<OrderWithProductsDTO> getOrderProductsOfOrders(List<Order> orders) {
        List<OrderWithProductsDTO> result = new ArrayList<>();
        for (Order order : orders) {
            List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderId(order);
            List<OrderProductDTO> orderProductDTOs = orderProducts.stream()
                    .map(orderProduct -> modelMapper.map(orderProduct, OrderProductDTO.class))
                    .collect(Collectors.toList());
            OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
            result.add(new OrderWithProductsDTO(orderDTO, orderProductDTOs));
        }
        return result;
    }

    public List<OrderProductDTO> getOrderProducts(Long orderId) throws ChangeSetPersister.NotFoundException {
        Order order = orderRepository.findById(orderId).orElseThrow(ChangeSetPersister.NotFoundException::new);
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrderId(order);
        return orderProducts.stream()
                .map(orderProduct -> modelMapper.map(orderProduct, OrderProductDTO.class))
                .collect(Collectors.toList());
    }

}
