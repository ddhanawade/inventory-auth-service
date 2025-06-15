package com.vehicle.authentication.inventory.config;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.sql.DataSource;

@Component
public class DataSourceShutdownHook {

    @Autowired
    private DataSource dataSource;

    @PreDestroy
    public void closeDataSource() {
        if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
            ((com.zaxxer.hikari.HikariDataSource) dataSource).close();
            System.out.println("HikariDataSource closed successfully.");
        }
    }
}
