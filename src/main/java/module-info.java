module h2orm {
    // Core Java modules
    requires java.base;
    requires java.sql;
    requires java.logging;
    requires java.desktop;

    // H2 Database
    requires com.h2database;

    // Jakarta Persistence API (transitive export for clients)
    requires transitive jakarta.persistence;

    // Jackson for JSON processing
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    // Apache POI for Excel export
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    // SLF4J for logging
    requires org.slf4j;

    // Apache Commons Math (automatic module)
    requires commons.math3;
    requires org.hibernate.orm.core;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Export main packages - SECURITY FIX: Removed example package export
    exports h2.orm;

    exports h2.orm.config;
    exports h2.orm.core;
    exports h2.orm.core.repository;
    exports h2.orm.core.service;


    // SECURITY FIX: Do NOT export example package
    // exports h2.orm.example; // REMOVED - Internal examples should not be public API

    // Open packages for reflection (needed for entity processing)
    opens h2.orm.config to com.fasterxml.jackson.databind;
    opens h2.orm.core to com.fasterxml.jackson.databind;

    // Open example package to Hibernate for entity reflection access (internal use only)
    opens h2.orm.example to org.hibernate.orm.core, jakarta.persistence;
}
