/**
 * H2-ORM Library Module
 *
 * A lightweight, Spring Boot-style ORM library for H2 database with support for multiple databases.
 * Provides easy-to-use repository pattern, automatic backup/export features, and transaction management.
 *
 * @author H2-ORM Team
 * @since 1.0.0
 */
module h2orm {

    // ===== REQUIRED DEPENDENCIES =====

    // JPA and Hibernate
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // Database drivers
    requires java.sql;
    requires com.h2database;

    // Jackson for JSON processing
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Apache POI for Excel export
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // Logging
    requires org.slf4j;
    requires ch.qos.logback.classic;

    // Connection pooling
    requires com.zaxxer.hikari;

    // Utilities
    requires java.base;
    requires java.naming;

    // ===== EXPORTED PACKAGES =====

    // Main API - what users will import
    exports h2.orm;

    // Configuration API
    exports h2.orm.config;

    // Repository API (Spring Boot style)
    exports h2.orm.core.repository;

    // Service APIs for advanced users
    exports h2.orm.core.service;

    // Exception types
    exports h2.orm.exception.types;

    // ===== OPENS FOR REFLECTION =====
    // Allow Hibernate and Jackson to access entities via reflection
    opens h2.orm.example to org.hibernate.orm.core, com.fasterxml.jackson.databind;
}
