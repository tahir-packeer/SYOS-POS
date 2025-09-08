# SYOS - Synex Outlet Store Point of Sale System

A comprehensive Java-based Point of Sale (POS) system designed for retail stores with inventory management, billing, and reporting capabilities.

## 🚀 Features

### Core Functionality

- **Multi-User System** with role-based access (Cashier, Manager, Admin)
- **Inventory Management** with shelf and website inventory tracking
- **Billing System** with manual discount support
- **Customer Management** with purchase history
- **Stock Management** with automatic reorder alerts
- **Reporting System** with sales analytics and inventory reports

### Advanced Features

- **Bill Template Generation** with automatic file saving
- **Manual Discount System** with validation
- **Transaction Types** (In-Store and Online)
- **Real-time Inventory Updates**
- **Professional Receipt Generation**

## 🛠️ Technology Stack

- **Java 11+**
- **MySQL Database**
- **Maven** for dependency management
- **JDBC** for database connectivity
- **HikariCP** for connection pooling
- **SLF4J + Logback** for logging
- **JUnit 5** for testing

## 📋 Prerequisites

- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6+

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/SYOS-POS.git
cd SYOS-POS
```

### 2. Database Setup

1. Create a MySQL database named `syos`
2. Update database credentials in `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/syos
db.username=your_username
db.password=your_password
```

### 3. Build the Project

```bash
mvn clean compile
```

### 4. Run the Application

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## 📁 Project Structure

```
SYOS-POS/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # User interface controllers
│   │   │   ├── dao/            # Data Access Objects
│   │   │   ├── model/          # Data models
│   │   │   ├── service/        # Business logic services
│   │   │   ├── util/           # Utility classes
│   │   │   └── view/           # User interface views
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback.xml
│   └── test/                   # Test files
├── bills/                      # Generated bill files
├── logs/                       # Application logs
├── pom.xml                     # Maven configuration
└── README.md
```

## 👥 User Roles

### Cashier

- Process in-store and online transactions
- View inventory levels
- Generate bills and receipts
- Manage bill files

### Manager

- All Cashier permissions
- View sales reports
- Manage stock levels
- Monitor inventory alerts

### Admin

- All Manager permissions
- User management
- System configuration
- Database maintenance

## 💰 Discount System

The system supports manual discount application:

- Enter custom discount amounts during billing
- Automatic validation (cannot exceed subtotal)
- Professional receipt display with discount breakdown
- Percentage calculation for display

## 📊 Reporting Features

- **Daily Sales Reports** with item-wise breakdown
- **Inventory Reports** with stock levels and alerts
- **Customer Purchase History**
- **Transaction Analysis** by type and date range

## 🧪 Testing

Run the test suite:

```bash
mvn test
```

## 📝 Configuration

Key configuration options in `application.properties`:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/syos
db.username=root
db.password=your_password

# Business Rules
business.reorder.threshold=50
business.discount.rate=0.0
business.tax.rate=0.0

# Application Settings
app.name=SYOS - Synex Outlet Store
app.version=1.0.0
```

## 🔧 Database Schema

The system uses the following main tables:

- `bills` - Transaction records
- `bill_items` - Individual items in transactions
- `customers` - Customer information
- `items` - Product catalog
- `shelf_stock` - In-store inventory
- `website_inventory` - Online inventory
- `users` - System users


## 🔄 Version History

- **v1.0.0** - Initial release with core POS functionality
- **v1.1.0** - Added bill template generation and file management
- **v1.2.0** - Implemented manual discount system
- **v1.3.0** - Enhanced reporting and inventory management

---

**SYOS-POS** - Professional Point of Sale System for Modern Retail
