package org.example.ecommerce_project.services;

import org.example.ecommerce_project.exception.AppException;
import org.example.ecommerce_project.repository.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_WhenProductNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() ->
                cartService.addToCart(1L, 99L, 1)
        );

        // Assert
        assertThat(thrown)
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(productRepo).findById(99L);
        verifyNoInteractions(inventoryService);
    }
}
