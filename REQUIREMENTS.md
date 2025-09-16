# Requirements and Installation Guide

## 1. Java Development Kit (JDK)

- **Version:** 11 or higher
- **Download:** [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)

## 2. Apache Tomcat (Servlet Container)

- **Version:** 9 or higher
- **Download:** [Tomcat Downloads](https://tomcat.apache.org/download-90.cgi)

## 3. MySQL Server

- **Version:** 8 or higher
- **Download:** [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

## 4. MySQL Workbench (Optional, for DB management)

- **Download:** [MySQL Workbench](https://dev.mysql.com/downloads/workbench/)

## 5. MySQL JDBC Driver

- **Version:** 8 or higher
- **Download:** [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)
- **Maven Dependency:**
  ```xml
  <dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
  </dependency>
  ```

## 6. Postman (API Testing)

- **Download:** [Postman](https://www.postman.com/downloads/)

## 7. IDE (Recommended)

- **IntelliJ IDEA** or **Eclipse IDE for Enterprise Java Developers**

## 8. Frontend (Simple)

- **Languages:** HTML, CSS, JavaScript
- **No frameworks required**
- **Run:** Open `index.html` in your browser

---

## Project Structure Example

```
/project-root
  /src
    /main
      /java
        /servlets
        /dao
        /model
      /webapp
        /WEB-INF
          web.xml
        index.html
        styles.css
        script.js
  pom.xml (if using Maven)
```

---

## Installation Steps

1. Install JDK, Tomcat, MySQL, and your IDE.
2. Set up your MySQL database and tables using MySQL Workbench.
3. Add the MySQL JDBC driver to your project (via Maven or manually).
4. Develop and deploy your Java servlets to Tomcat.
5. Test your endpoints using Postman.
6. Create a simple frontend using HTML, CSS, and JS (no build tools needed).

---

**You are now ready to develop and run your POS web application!**
