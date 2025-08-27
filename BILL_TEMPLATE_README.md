# Bill Template and File Management System

## Overview

The SYOS-POS system now includes comprehensive bill template generation and file management functionality. After each purchase, bills are automatically saved as text files for record-keeping and customer service purposes.

## Features

### 1. Bill Template Generation

- **Professional Receipt Format**: Bills are formatted as official receipts with company branding
- **Complete Information**: Includes bill details, customer information, itemized purchases, and payment details
- **Automatic Calculation**: Subtotal, discount, total amount, cash received, and change are automatically calculated

### 2. Automatic File Saving

- **File Naming Convention**: `Bill_{BillID}_Serial_{SerialNumber}_{Date}_{Time}.txt`
- **Organized Storage**: All bills are saved in a `bills/` directory
- **Automatic Creation**: The bills directory is created automatically if it doesn't exist

### 3. Bill Management Interface

- **View Bills by ID**: Retrieve and display specific bills
- **View Bills by Serial Number**: Find bills using their unique serial number
- **View Bills by Date**: List all bills for a specific date
- **View Bills by Customer**: Show all bills for a specific customer
- **View Bills by Transaction Type**: Filter bills by IN_STORE or ONLINE transactions
- **List Saved Files**: View all saved bill files with their details

## Bill Template Format

```
==================================================
              SYOS - SYNEX OUTLET STORE
==================================================
              OFFICIAL RECEIPT
==================================================

Bill ID: 123
Serial No: 1001
Date: 15/01/2024 14:30:25
Transaction Type: IN_STORE
Status: COMPLETED

Customer Details:
------------------------------
Name: John Doe
Phone: 1234567890

Items Purchased:
--------------------------------------------------
Code     Item Name              Qty     Price     Total
--------------------------------------------------
ITEM001  Test Product 1          2   Rs.  10.50 Rs.  21.00
ITEM002  Test Product 2          1   Rs.  25.00 Rs.  25.00
--------------------------------------------------
                                                                         Subtotal: Rs.  46.00
                                     Discount: Rs.   5.00 (10.9%)
                                 Total Amount: Rs.  41.00
                              Cash Received: Rs.  50.00
                                                                         Change: Rs.   9.00
--------------------------------------------------

Thank you for shopping with SYOS!
Please keep this receipt for your records.
For any queries, please contact us.

Generated on: 15/01/2024 14:30:25
==================================================
```

## Discount System

### Manual Discount Only

The system now supports **manual discount application only**. No automatic discounts are applied.

### Discount Features

1. **Manual Input**: Users must manually enter discount amounts
2. **Validation**: System validates that discounts don't exceed subtotal
3. **Display**: Discount amounts and percentages are shown in receipts
4. **Flexibility**: Any discount amount can be applied (within valid limits)

## Usage

### For Cashiers, Managers, and Admins

1. **Process a Sale**: Complete a normal billing transaction
2. **Automatic Generation**: The bill template is automatically generated and saved
3. **Display Receipt**: The formatted receipt is displayed on screen
4. **File Location**: The file path is shown for reference

### Bill Management Menu

Access the Bill Management menu from:

- **Cashier Menu**: Option 3
- **Manager Menu**: Option 6
- **Admin Menu**: Option 7

#### Available Options:

1. **View Bill by ID**: Enter a bill ID to view its details
2. **View Bill by Serial Number**: Enter a serial number to find a bill
3. **View Bills by Date**: Enter a date (dd/MM/yyyy) to see all bills for that date
4. **View Bills by Customer**: Enter a customer ID to see their purchase history
5. **View Bills by Transaction Type**: Choose IN_STORE or ONLINE to filter bills
6. **List All Saved Bill Files**: View all saved bill files with their details

## File Storage

### Directory Structure

```
SYOS-POS/
├── bills/                          # Bill files directory
│   ├── Bill_1_Serial_1001_2024-01-15_14-30-25.txt
│   ├── Bill_2_Serial_1002_2024-01-15_15-45-10.txt
│   └── ...
├── src/
├── target/
└── ...
```

### File Naming Convention

- **Format**: `Bill_{BillID}_Serial_{SerialNumber}_{Date}_{Time}.txt`
- **Example**: `Bill_123_Serial_1001_2024-01-15_14-30-25.txt`
- **Components**:
  - Bill ID: Database primary key
  - Serial Number: Unique bill serial number
  - Date: YYYY-MM-DD format
  - Time: HH-MM-SS format

## Technical Implementation

### Key Classes

1. **BillTemplateService**: Core service for template generation and file management
2. **BillManagementController**: Controller for bill management operations
3. **BillingService**: Enhanced to automatically generate and save bills
4. **BillingController**: Updated to display bill templates after transactions

### Integration Points

- **Billing Process**: Automatically generates and saves bills after successful transactions
- **User Interfaces**: Added bill management options to all user roles
- **File System**: Creates and manages the bills directory automatically
- **Database**: Retrieves bill and customer data for template generation

## Benefits

1. **Record Keeping**: All transactions are automatically saved as files
2. **Customer Service**: Easy retrieval of past transactions
3. **Audit Trail**: Complete history of all sales transactions
4. **Professional Presentation**: Well-formatted receipts for customers
5. **Data Backup**: Additional backup of transaction data in file format
6. **Easy Access**: Simple interface to find and view any bill

## Error Handling

- **Missing Bills**: Graceful handling when bills are not found
- **File System Errors**: Proper error logging and user notification
- **Database Errors**: Fallback mechanisms for data retrieval issues
- **Invalid Input**: Input validation for all user inputs

## Future Enhancements

1. **PDF Generation**: Convert text files to PDF format
2. **Email Integration**: Send receipts via email
3. **Printing Support**: Direct printing of receipts
4. **Digital Signatures**: Add digital signatures to receipts
5. **QR Codes**: Include QR codes for digital verification
6. **Multi-language Support**: Support for multiple languages
7. **Custom Templates**: Allow customization of receipt templates
