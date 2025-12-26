package org.example.ecommerce_project.services;

import org.example.ecommerce_project.entity.Inventory;
import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.InventoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    InventoryRepo inventoryRepository;

    @InjectMocks
    InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        // nothing
    }

    @Test
    void reserveStock_whenInStockGteQty_shouldDecreaseStockAndSave() {
        // Arrange
        long productId = 10L;
        int inStock = 7;
        int qty = 3;

        Inventory inv = new Inventory();
        inv.setProductId(productId);   // om ni har productId som PK
        inv.setInStock(inStock);

        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        inventoryService.reserveStock(productId, qty);

        // Assert: stock minskar korrekt
        assertThat(inv.getInStock()).isEqualTo(inStock - qty);

        // Assert: save körs med rätt värde
        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(captor.capture());
        assertThat(captor.getValue().getProductId()).isEqualTo(productId);
        assertThat(captor.getValue().getInStock()).isEqualTo(inStock - qty);

        verify(inventoryRepository).findById(productId);
        verifyNoMoreInteractions(inventoryRepository);
    }

    @Test
    void reserveStock_whenInStockLtQty_shouldThrow_andNotSave() {
        long productId = 10L;
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setInStock(2);

        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> inventoryService.reserveStock(productId, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock")
                .hasMessageContaining("product id: 10")
                .hasMessageContaining("Available: 2")
                .hasMessageContaining("requested: 3");

        assertThat(inv.getInStock()).isEqualTo(2);
        verify(inventoryRepository, never()).save(any());
        verify(inventoryRepository).findById(productId);
        verifyNoMoreInteractions(inventoryRepository);
    }



    @Test
    void reserveStock_whenQtyInvalid_shouldThrow_andNotTouchRepository() {
        assertThatThrownBy(() -> inventoryService.reserveStock(10L, 0))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("quantity must be positive");

        verifyNoInteractions(inventoryRepository);
    }

}
