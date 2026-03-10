package com.example.shopee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

/**
 * Shopee Clone - Main Application Entry Point
 *
 * Uses Spring Modulith for modular monolithic architecture
 * Each module (product, user, cart, etc.) operates independently
 * while maintaining clear API contracts
 */
@Modulith
@SpringBootApplication
public class ShopeeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopeeApplication.class, args);
	}
}

