package com.vehicle.authentication.inventory.service;

import com.vehicle.authentication.inventory.config.DatasourceProperties;
import org.springframework.stereotype.Service;

@Service
public class PropertyReaderService {

    private final DatasourceProperties datasourceProperties;

    public PropertyReaderService(DatasourceProperties datasourceProperties) {
        this.datasourceProperties = datasourceProperties;
    }

    public void printProperties() {
        System.out.println("Datasource URL: " + datasourceProperties.getUrl());
        System.out.println("Datasource Username: " + datasourceProperties.getUsername());
        System.out.println("Datasource Password: " + datasourceProperties.getPassword());
    }
}