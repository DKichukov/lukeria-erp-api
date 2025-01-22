package com.example.ludogoriesoft.lukeriaerpapi.services;

import com.example.ludogoriesoft.lukeriaerpapi.dtos.InvoiceOrderProductConfigDTO;
import com.example.ludogoriesoft.lukeriaerpapi.dtos.InvoiceOrderProductDTO;
import com.example.ludogoriesoft.lukeriaerpapi.models.Invoice;
import com.example.ludogoriesoft.lukeriaerpapi.models.InvoiceOrderProduct;
import com.example.ludogoriesoft.lukeriaerpapi.models.Order;
import com.example.ludogoriesoft.lukeriaerpapi.models.OrderProduct;
import com.example.ludogoriesoft.lukeriaerpapi.repository.*;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InvoiceOrderProductServiceTest {

    @Mock
    private InvoiceOrderProductRepository invoiceOrderProductRepository;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private OrderProductService orderProductService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ClientQueryService clientQueryService;

    @InjectMocks
    private InvoiceOrderProductService invoiceOrderProductService;
    @Captor
    private ArgumentCaptor<InvoiceOrderProduct> invoiceOrderProductCaptor;
    @Captor
    private ArgumentCaptor<OrderProduct> orderProductCaptor;

    @Captor
    private ArgumentCaptor<InvoiceOrderProductDTO> invoiceOrderProductDTOCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testFindInvoiceOrderProductsByInvoiceId() {
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        InvoiceOrderProduct invoiceOrderProduct = new InvoiceOrderProduct();
        invoiceOrderProduct.setInvoiceId(invoice);
        List<InvoiceOrderProduct> mockInvoiceOrderProductsList = new ArrayList<>();
        mockInvoiceOrderProductsList.add(invoiceOrderProduct);
        mockInvoiceOrderProductsList.add(invoiceOrderProduct);
        mockInvoiceOrderProductsList.add(invoiceOrderProduct);


        Mockito.when(invoiceOrderProductRepository.findAll()).thenReturn(mockInvoiceOrderProductsList);
        Mockito.when(orderProductService.findInvoiceOrderProductsByInvoiceId(1L)).thenReturn(mockInvoiceOrderProductsList);

        List<InvoiceOrderProduct> result = orderProductService.findInvoiceOrderProductsByInvoiceId(1L);

        assertEquals(3, result.size());
    }

    @Test
    void testFindInvoiceOrderProductsByInvoiceIdWhenNoMatchingProducts() {
        List<InvoiceOrderProduct> mockInvoiceOrderProductsList = new ArrayList<>();

        Mockito.when(invoiceOrderProductRepository.findAll()).thenReturn(mockInvoiceOrderProductsList);

        List<InvoiceOrderProduct> result = orderProductService.findInvoiceOrderProductsByInvoiceId(1L);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testValidateInvoiceOrderProduct_ThrowsValidationExceptionWhenOrderProductIdIsNull() {
        // Arrange
        InvoiceOrderProductDTO invoiceOrderProductDTO = new InvoiceOrderProductDTO();
        invoiceOrderProductDTO.setOrderProductId(null);

        // Act and Assert
        assertThrows(ValidationException.class, () -> invoiceOrderProductService.validateInvoiceOrderProduct(invoiceOrderProductDTO));
    }

    @Test
    void testCreateInvoiceOrderProduct_ThrowsValidationExceptionWhenOrderProductIdIsNull() {
        // Arrange
        InvoiceOrderProductDTO invoiceOrderProductDTO = new InvoiceOrderProductDTO();
        invoiceOrderProductDTO.setOrderProductId(null);
        invoiceOrderProductDTO.setInvoiceId(null);


        // Verify method calls
        verify(orderProductRepository, never()).existsById(anyLong());
        verify(invoiceRepository, never()).existsById(anyLong());
        verify(invoiceOrderProductRepository, never()).save(any());
    }

    @Test
    void testGetAllInvoiceOrderProducts() {
        // Arrange
        List<InvoiceOrderProduct> mockInvoiceOrderProducts = new ArrayList<>();
        mockInvoiceOrderProducts.add(new InvoiceOrderProduct(/* Add sample data here */));

        when(invoiceOrderProductRepository.findByDeletedFalse()).thenReturn(mockInvoiceOrderProducts);

        List<InvoiceOrderProductDTO> mockInvoiceOrderProductDTOs = new ArrayList<>();
        mockInvoiceOrderProductDTOs.add(new InvoiceOrderProductDTO(/* Add sample data here */));

        when(modelMapper.map(any(InvoiceOrderProduct.class), eq(InvoiceOrderProductDTO.class)))
                .thenReturn(mockInvoiceOrderProductDTOs.get(0));

        // Act
        List<InvoiceOrderProductDTO> result = invoiceOrderProductService.getAllInvoiceOrderProducts();

        // Assert
        assertEquals(mockInvoiceOrderProductDTOs, result);
        // You can also add more specific assertions based on your use case.
    }

    @Test
    void testGetInvoiceOrderProductById_ExistingId_ReturnsDTO() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long existingId = 1L;
        InvoiceOrderProduct mockInvoiceOrderProduct = new InvoiceOrderProduct(/* Add sample data here */);
        when(invoiceOrderProductRepository.findByIdAndDeletedFalse(existingId)).thenReturn(Optional.of(mockInvoiceOrderProduct));

        InvoiceOrderProductDTO mockInvoiceOrderProductDTO = new InvoiceOrderProductDTO(/* Add sample data here */);
        when(modelMapper.map(mockInvoiceOrderProduct, InvoiceOrderProductDTO.class))
                .thenReturn(mockInvoiceOrderProductDTO);

        // Act
        InvoiceOrderProductDTO result = invoiceOrderProductService.getInvoiceOrderProductById(existingId);

        // Assert
        Assertions.assertNotNull(result);
        assertEquals(mockInvoiceOrderProductDTO, result);
        // You can add more specific assertions based on your use case.
    }

    @Test
    void testGetInvoiceOrderProductById_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        Long nonExistingId = 100L;
        when(invoiceOrderProductRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> invoiceOrderProductService.getInvoiceOrderProductById(nonExistingId));
    }

    @Test
    void testDeleteInvoiceOrderProduct_ExistingId_DeletesProduct() throws ChangeSetPersister.NotFoundException {
        // Arrange
        Long existingId = 1L;
        InvoiceOrderProduct mockInvoiceOrderProduct = new InvoiceOrderProduct(/* Add sample data here */);
        when(invoiceOrderProductRepository.findByIdAndDeletedFalse(existingId)).thenReturn(Optional.of(mockInvoiceOrderProduct));

        // Act
        invoiceOrderProductService.deleteInvoiceOrderProduct(existingId);

        // Assert
        Assertions.assertTrue(mockInvoiceOrderProduct.isDeleted());
        verify(invoiceOrderProductRepository, times(1)).save(mockInvoiceOrderProduct);
        // You can add more specific assertions based on your use case.
    }

    @Test
    void testDeleteInvoiceOrderProduct_NonExistingId_ThrowsNotFoundException() {
        // Arrange
        Long nonExistingId = 100L;
        when(invoiceOrderProductRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ChangeSetPersister.NotFoundException.class,
                () -> invoiceOrderProductService.deleteInvoiceOrderProduct(nonExistingId));
        verify(invoiceOrderProductRepository, never()).save(any());
    }


    @Test
    void testValidateInvoiceOrderProduct_ValidDTO_NoExceptionsThrown() {
        // Arrange
        InvoiceOrderProductDTO validDTO = new InvoiceOrderProductDTO();
        validDTO.setOrderProductId(1L);
        validDTO.setInvoiceId(2L);

        when(orderProductRepository.existsById(validDTO.getOrderProductId())).thenReturn(true);
        when(invoiceRepository.existsById(validDTO.getInvoiceId())).thenReturn(true);

        // Act and Assert
        assertDoesNotThrow(() -> invoiceOrderProductService.validateInvoiceOrderProduct(validDTO));
    }

    @Test
    void testValidateInvoiceOrderProduct_NullOrderProductId_ThrowsValidationException() {
        // Arrange
        InvoiceOrderProductDTO invalidDTO = new InvoiceOrderProductDTO();
        invalidDTO.setInvoiceId(2L);

        // Act and Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> invoiceOrderProductService.validateInvoiceOrderProduct(invalidDTO));
        assertEquals("OrderProduct ID cannot be null!", exception.getMessage());
    }

    @Test
    void testValidateInvoiceOrderProduct_NullInvoiceId_ThrowsValidationException() {
        // Arrange
        InvoiceOrderProductDTO invalidDTO = new InvoiceOrderProductDTO();
        invalidDTO.setOrderProductId(1L);
        when(orderProductRepository.existsById(invalidDTO.getOrderProductId())).thenReturn(true);
        // Act and Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> invoiceOrderProductService.validateInvoiceOrderProduct(invalidDTO));
        assertEquals("Invoice ID cannot be null!", exception.getMessage());
    }

    @Test
    void testValidateInvoiceOrderProduct_NonExistingOrderProductId_ThrowsValidationException() {
        // Arrange
        InvoiceOrderProductDTO invalidDTO = new InvoiceOrderProductDTO();
        invalidDTO.setOrderProductId(1L);
        invalidDTO.setInvoiceId(2L);

        when(orderProductRepository.existsById(invalidDTO.getOrderProductId())).thenReturn(false);

        // Act and Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> invoiceOrderProductService.validateInvoiceOrderProduct(invalidDTO));
        assertEquals("OrderProduct does not exist with ID: 1", exception.getMessage());
    }

    @Test
    void testValidateInvoiceOrderProduct_NonExistingInvoiceId_ThrowsValidationException() {
        // Arrange
        InvoiceOrderProductDTO invalidDTO = new InvoiceOrderProductDTO();
        invalidDTO.setOrderProductId(1L);
        invalidDTO.setInvoiceId(2L);

        when(orderProductRepository.existsById(invalidDTO.getOrderProductId())).thenReturn(true);
        when(invoiceRepository.existsById(invalidDTO.getInvoiceId())).thenReturn(false);

        // Act and Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> invoiceOrderProductService.validateInvoiceOrderProduct(invalidDTO));
        assertEquals("Invoice does not exist with ID: 2", exception.getMessage());
    }

    @Test
    void testUpdateInvoiceOrderProduct_ValidInput_ReturnsDTO() throws ChangeSetPersister.NotFoundException {
        Long id = 1L;
        InvoiceOrderProductDTO inputDTO = new InvoiceOrderProductDTO();
        inputDTO.setOrderProductId(1L);
        inputDTO.setInvoiceId(1L);
        Invoice invoice = new Invoice();
        invoice.setId(1L);
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setId(1L);
        Invoice invoice2 = new Invoice();
        invoice.setId(2L);
        OrderProduct orderProduct2 = new OrderProduct();
        orderProduct.setId(2L);

        InvoiceOrderProduct existingInvoiceOrderProduct = new InvoiceOrderProduct();
        existingInvoiceOrderProduct.setId(id);
        existingInvoiceOrderProduct.setOrderProductId(orderProduct);
        existingInvoiceOrderProduct.setInvoiceId(invoice);
        existingInvoiceOrderProduct.setDeleted(false);

        when(invoiceOrderProductRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(existingInvoiceOrderProduct));

        when(orderProductRepository.existsById(inputDTO.getOrderProductId())).thenReturn(true);
        when(invoiceRepository.existsById(inputDTO.getInvoiceId())).thenReturn(true);

        InvoiceOrderProduct updatedInvoiceOrderProduct = new InvoiceOrderProduct();
        updatedInvoiceOrderProduct.setId(id);
        updatedInvoiceOrderProduct.setOrderProductId(orderProduct2);
        updatedInvoiceOrderProduct.setInvoiceId(invoice2);
        updatedInvoiceOrderProduct.setDeleted(false);

        when(modelMapper.map(inputDTO, InvoiceOrderProduct.class)).thenReturn(updatedInvoiceOrderProduct);

        when(invoiceOrderProductRepository.save(updatedInvoiceOrderProduct))
                .thenReturn(updatedInvoiceOrderProduct);

        InvoiceOrderProductDTO expectedDTO = new InvoiceOrderProductDTO();
        expectedDTO.setId(id);
        expectedDTO.setOrderProductId(1L);
        expectedDTO.setInvoiceId(2L);
        expectedDTO.setDeleted(false);

        when(modelMapper.map(updatedInvoiceOrderProduct, InvoiceOrderProductDTO.class))
                .thenReturn(expectedDTO);
        InvoiceOrderProductDTO result = invoiceOrderProductService.updateInvoiceOrderProduct(id, inputDTO);
        Assertions.assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(1L, result.getOrderProductId());
        assertEquals(2L, result.getInvoiceId());
        Assertions.assertFalse(result.isDeleted());
    }

    @Test
    void testCreateInvoiceOrderProduct() {
        InvoiceOrderProductDTO invoiceOrderProductDTO = new InvoiceOrderProductDTO();
        invoiceOrderProductDTO.setOrderProductId(1L);
        invoiceOrderProductDTO.setId(1L);
        when(invoiceOrderProductRepository.save(any(InvoiceOrderProduct.class))).thenReturn(new InvoiceOrderProduct());
        when(orderRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(new Order()));
        InvoiceOrderProductDTO result = invoiceOrderProductService.createInvoiceOrderProduct(invoiceOrderProductDTO);
        verify(invoiceOrderProductRepository).save(invoiceOrderProductCaptor.capture());
        InvoiceOrderProduct savedInvoiceOrderProduct = invoiceOrderProductCaptor.getValue();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testcreateInvoiceOrderProductWithIds() {
        InvoiceOrderProductConfigDTO configDTO = new InvoiceOrderProductConfigDTO();
        List<Long> orderProducts = new ArrayList<>();
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setId(1L);
        orderProducts.add(orderProduct.getId());
        configDTO.setOrderProductIds(orderProducts);
        List<BigDecimal> prices = new ArrayList<>();
        prices.add(BigDecimal.valueOf(10));
        configDTO.setPriceInputBigDecimalList(prices);
        List<Integer> qualities = new ArrayList<>();
        qualities.add(10);
        configDTO.setQuantityInputIntList(qualities);
        when(orderProductRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.of(new OrderProduct()));
        when(invoiceOrderProductRepository.save(any(InvoiceOrderProduct.class))).thenReturn(new InvoiceOrderProduct());
        String result = invoiceOrderProductService.createInvoiceOrderProductWithIds(configDTO);
        verify(orderProductRepository, times(configDTO.getOrderProductIds().size())).save(any(OrderProduct.class));
    }

    @Test
    void testcreateInvoiceOrderProductWithIdsWithInvalidOrderProduct() {
        InvoiceOrderProductConfigDTO configDTO = new InvoiceOrderProductConfigDTO();
        List<Long> orderProducts = new ArrayList<>();
        orderProducts.add(1L);
        configDTO.setOrderProductIds(orderProducts);
        when(orderProductRepository.findByIdAndDeletedFalse(anyLong())).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> invoiceOrderProductService.createInvoiceOrderProductWithIds(configDTO));
        assertEquals("Записът не е намерен за orderProductId: 1", exception.getMessage());
    }
}
