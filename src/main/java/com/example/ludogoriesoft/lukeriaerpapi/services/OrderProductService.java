package com.example.ludogoriesoft.lukeriaerpapi.services;

import com.example.ludogoriesoft.lukeriaerpapi.dtos.OrderProductDTO;
import com.example.ludogoriesoft.lukeriaerpapi.dtos.PlateDTO;
import com.example.ludogoriesoft.lukeriaerpapi.models.*;
import com.example.ludogoriesoft.lukeriaerpapi.models.Package;
import com.example.ludogoriesoft.lukeriaerpapi.repository.*;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderProductService {

    private final InvoiceOrderProductRepository invoiceOrderProductRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;
    private final PackageRepository packageRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

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
    private final PlateRepository plateRepository;
    private final CartonRepository cartonRepository;
    public boolean reduceProducts(List<InvoiceOrderProduct> invoiceOrderProductsList){
        for (InvoiceOrderProduct invoiceOrderProduct : invoiceOrderProductsList) {
            Optional<Package> packageForReduce = packageRepository.findByIdAndDeletedFalse(invoiceOrderProduct.getOrderProductId().getPackageId().getId());
            int sellingPackageForReduce = invoiceOrderProduct.getOrderProductId().getNumber();
            if (packageForReduce.isPresent()) {
                Package aPackage = packageForReduce.get();
                aPackage.setAvailableQuantity(aPackage.getAvailableQuantity() - sellingPackageForReduce);
                Optional<Carton> carton = cartonRepository.findByIdAndDeletedFalse(aPackage.getCartonId().getId());
                double cartonStock = (double) sellingPackageForReduce / aPackage.getPiecesCarton();
                carton.get().setAvailableQuantity((int) Math.ceil(carton.get().getAvailableQuantity() - cartonStock));
                Optional<Plate> plate = plateRepository.findByIdAndDeletedFalse(aPackage.getPlateId().getId());
                plate.get().setAvailableQuantity(plate.get().getAvailableQuantity() - sellingPackageForReduce);
                Optional<Product> product = productRepository.findByPackageIdAndDeletedFalse(aPackage);
                product.get().setAvailableQuantity(product.get().getAvailableQuantity() - sellingPackageForReduce);
                packageRepository.save(aPackage);
            } else return false;
        }
        return true;
    }
}
