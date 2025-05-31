package com.vehicle.authentication.inventory;

import com.vehicle.authentication.inventory.config.DatasourceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("local")
public class LocalProfileTest {

    @Autowired
    private DatasourceProperties datasourceProperties;

    @Test
    public void testLocalProfileConfiguration() {
        assertEquals("jdbc:mysql://localhost:3306/fleet_manager?useSSL=false&allowPublicKeyRetrieval=true", datasourceProperties.getUrl());
        assertEquals("root", datasourceProperties.getUsername());
        assertEquals("Ded12jky+", datasourceProperties.getPassword());
    }
}