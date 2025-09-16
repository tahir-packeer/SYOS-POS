package org.example;

import org.example.config.DatabaseConnectionFactory;
import org.example.controller.*;
import org.example.dao.*;
import org.example.dao.impl.*;
import org.example.service.*;
import org.example.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Main application class with dependency injection pattern
 * Implements proper layered architecture and clean code principles
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting SYOS Application...");

        try (Scanner scanner = new Scanner(System.in)) {
            // Initialize application
            Application app = new Application();
            app.start(scanner);
        } catch (Exception e) {
            logger.error("Fatal error in application", e);
            System.err.println("Application failed to start: " + e.getMessage());
            System.exit(1);
        } finally {
            // Cleanup resources
            DatabaseConnectionFactory.getInstance().closeDataSource();
            logger.info("SYOS Application terminated.");
        }
    }

    /**
     * Application class that handles dependency injection and initialization
     */
    private static class Application {
        private final Logger logger = LoggerFactory.getLogger(Application.class);

        // DAOs
        private UserDAO userDAO;
        private CustomerDAO customerDAO;
        private ItemDAO itemDAO;
        private BillDAO billDAO;
        private ShelfStockDAO shelfStockDAO;
        private WebsiteInventoryDAO websiteInventoryDAO;
        private StockBatchDAO stockBatchDAO;

        // Services
        private AuthenticationService authService;
        private BillingService billingService;
        private StockService stockService;
        private ReportService reportService;

        // Controllers
        private AuthenticationController authController;
        private BillingController billingController;
        private StockController stockController;
        private ReportController reportController;
        private ItemController itemController;
        private BillManagementController billManagementController;

        // Views
        private MainView mainView;
        private CashierView cashierView;
        private ManagerView managerView;
        private AdminView adminView;

        public void start(Scanner scanner) {
            try {
                initializeComponents(scanner);
                logger.info("Application components initialized successfully");

                // Start the main application
                mainView.start(scanner);

            } catch (Exception e) {
                logger.error("Error starting application", e);
                throw new RuntimeException("Failed to start application", e);
            }
        }

        private void initializeComponents(Scanner scanner) throws SQLException {
            // Initialize database connection
            Connection connection = DatabaseConnectionFactory.getInstance().getConnection();
            logger.info("Database connection established");

            // Initialize DAOs
            initializeDAOs(connection);
            logger.debug("DAOs initialized");

            // Initialize Services
            initializeServices();
            logger.debug("Services initialized");

            // Initialize Controllers
            initializeControllers(scanner);
            logger.debug("Controllers initialized");

            // Initialize Views
            initializeViews();
            logger.debug("Views initialized");
        }

        private void initializeDAOs(Connection connection) {
            userDAO = new UserDAOImpl(connection);
            customerDAO = new CustomerDAOImpl(connection);
            itemDAO = new ItemDAOImpl(connection);
            billDAO = new BillDAOImpl(connection);
            shelfStockDAO = new ShelfStockDAOImpl(connection);
            websiteInventoryDAO = new WebsiteInventoryDAOImpl(connection);
            stockBatchDAO = new StockBatchDAOImpl(connection);
        }

        private void initializeServices() {
            authService = new AuthenticationService(userDAO);
            billingService = new BillingService(billDAO, customerDAO, shelfStockDAO,
                    websiteInventoryDAO, itemDAO);
            stockService = new StockService(stockBatchDAO, shelfStockDAO, websiteInventoryDAO);
            reportService = new ReportService(billDAO, itemDAO, shelfStockDAO,
                    websiteInventoryDAO, stockBatchDAO, customerDAO);
        }

        private void initializeControllers(Scanner scanner) {
            authController = new AuthenticationController(authService);
            billingController = new BillingController(billingService);
            stockController = new StockController(stockService);
            reportController = new ReportController(reportService);
            itemController = new ItemController(itemDAO);
            billManagementController = new BillManagementController(billingService, billDAO, scanner);
        }

        private void initializeViews() {
            cashierView = new CashierView(billingController, authController, billManagementController);
            managerView = new ManagerView(billingController, stockController, reportController,
                    itemController, authController, billManagementController);
            adminView = new AdminView(billingController, stockController, reportController,
                    itemController, authController, billManagementController);
            mainView = new MainView(authController, cashierView, managerView, adminView);
        }
    }
}
