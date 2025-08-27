// File: src/main/java/org/example/dao/CustomerDAO.java
package org.example.dao;

import org.example.model.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    Optional<Customer> getCustomerByPhone(String phone);
    Optional<Customer> getCustomerById(int customerId);
    List<Customer> getAllCustomers();
    int addCustomer(Customer customer);
    boolean updateCustomer(Customer customer);
    List<Customer> getCustomersByTransactionType(String transactionType);
}