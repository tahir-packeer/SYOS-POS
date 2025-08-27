package org.example.service;

import org.example.dao.BillDAO;
import org.example.dao.CustomerDAO;
import org.example.model.Bill;
import org.example.model.BillItem;
import org.example.model.Customer;
import org.example.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Test class for BillTemplateService
 */
public class BillTemplateServiceTest {

    @Mock
    private BillDAO billDAO;

    @Mock
    private CustomerDAO customerDAO;

    private BillTemplateService billTemplateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        billTemplateService = new BillTemplateService(billDAO, customerDAO);
    }

    @Test
    void testGenerateBillTemplateWithDiscount() {
        // Create test data
        Customer customer = new Customer(1, "1234567890", "John Doe");
        BillItem item1 = new BillItem("ITEM001", "Test Product 1", 2, 10.50);
        BillItem item2 = new BillItem("ITEM002", "Test Product 2", 1, 25.00);

        Bill bill = new Bill(123, 1001, new Date(), 1,
                Arrays.asList(item1, item2), 50.00, TransactionType.IN_STORE, 5.00);

        // Mock customer DAO
        when(customerDAO.getCustomerById(1)).thenReturn(Optional.of(customer));

        // Generate template
        String template = billTemplateService.generateBillTemplate(bill);

        // Verify discount is printed
        assertTrue(template.contains("Discount:"), "Discount line should be present");
        assertTrue(template.contains("Rs.   5.00"), "Discount amount should be present");
        assertTrue(template.contains("(10.9%)"), "Discount percentage should be present");

        // Verify totals are correct
        assertTrue(template.contains("Subtotal:                              Rs.  46.00"));
        assertTrue(template.contains("Total Amount:                          Rs.  41.00"));
        assertTrue(template.contains("Cash Received:                         Rs.  50.00"));
        assertTrue(template.contains("Change:                                Rs.   9.00"));
    }

    @Test
    void testGenerateBillTemplateWithoutDiscount() {
        // Create test data
        Customer customer = new Customer(1, "1234567890", "John Doe");
        BillItem item1 = new BillItem("ITEM001", "Test Product 1", 2, 10.50);
        BillItem item2 = new BillItem("ITEM002", "Test Product 2", 1, 25.00);

        Bill bill = new Bill(123, 1001, new Date(), 1,
                Arrays.asList(item1, item2), 46.00, TransactionType.IN_STORE, 0.0);

        // Mock customer DAO
        when(customerDAO.getCustomerById(1)).thenReturn(Optional.of(customer));

        // Generate template
        String template = billTemplateService.generateBillTemplate(bill);

        // Verify discount is printed as 0.00
        assertTrue(template.contains("Discount:"), "Discount line should be present");
        assertTrue(template.contains("Rs.   0.00"), "Discount should be 0.00");
        assertFalse(template.contains("(0.0%)"), "No percentage should be shown for 0 discount");

        // Verify totals are correct
        assertTrue(template.contains("Subtotal:                              Rs.  46.00"));
        assertTrue(template.contains("Total Amount:                          Rs.  46.00"));
        assertTrue(template.contains("Cash Received:                         Rs.  46.00"));
        assertTrue(template.contains("Change:                                Rs.   0.00"));
    }

    @Test
    void testGenerateBillTemplateWithLargeDiscount() {
        // Create test data with large discount
        Customer customer = new Customer(1, "1234567890", "John Doe");
        BillItem item1 = new BillItem("ITEM001", "Expensive Item", 1, 1000.00);

        Bill bill = new Bill(123, 1001, new Date(), 1,
                Arrays.asList(item1), 500.00, TransactionType.IN_STORE, 500.00);

        // Mock customer DAO
        when(customerDAO.getCustomerById(1)).thenReturn(Optional.of(customer));

        // Generate template
        String template = billTemplateService.generateBillTemplate(bill);

        // Verify discount is printed correctly
        assertTrue(template.contains("Discount:"), "Discount line should be present");
        assertTrue(template.contains("Rs. 500.00"), "Discount amount should be present");
        assertTrue(template.contains("(50.0%)"), "Discount percentage should be 50%");

        // Verify totals are correct
        assertTrue(template.contains("Subtotal:                              Rs. 1000.00"));
        assertTrue(template.contains("Total Amount:                          Rs.  500.00"));
        assertTrue(template.contains("Cash Received:                         Rs.  500.00"));
        assertTrue(template.contains("Change:                                Rs.   0.00"));
    }
}
