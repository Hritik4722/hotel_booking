package com.hotelbooking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Ensures initial sample data for 6 Indian hotels exists in the database.
 * Executes idempotently on startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hotels", Integer.class);
            if (count == null || count == 0) {
                log.info("Hotels table is empty. Seeding initial hotel records...");
                seedHotels();
                log.info("Hotel seeding completed successfully.");
            } else {
                log.info("Hotels table already contains {} records. Skipping seeding.", count);
            }
        } catch (Exception e) {
            log.warn("Could not verify or seed hotels table (will rely on schema.sql/data.sql or subsequent migrations): {}", e.getMessage());
        }
    }

    private void seedHotels() {
        String insertSql = """
            INSERT INTO hotels (id, name, location, description, amenities, price_per_night, total_rooms)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE name=VALUES(name)
        """;

        Object[][] sampleHotels = {
            {
                1L,
                "The Grand Heritage Palace",
                "Udaipur",
                "A majestic royal retreat nestled on the tranquil banks of Lake Pichola with panoramic views of the Aravalli hills and authentic Mewar architecture.",
                "Free High-Speed WiFi, Infinity Lake-View Pool, Royal Spa, Fine Dining Restaurant, Valet Parking, Cocktail Lounge",
                new BigDecimal("7500.00"),
                4
            },
            {
                2L,
                "Taj Vista Coastal Resort",
                "Goa",
                "A luxury beachfront haven surrounded by swaying palms and golden sands, offering seaside cabanas, water sports, and sunset cocktail lounges.",
                "Direct Beach Access, Oceanfront Pool, Ayurvedic Spa, Seafood Bistro, Free WiFi, Yoga Deck, Beach Bar",
                new BigDecimal("5200.00"),
                5
            },
            {
                3L,
                "The Royal Amber Haveli",
                "Jaipur",
                "An authentic Rajasthani heritage mansion located in the Pink City featuring traditional courtyards, hand-painted frescoes, and evening cultural shows.",
                "Complimentary Royal Breakfast, Rooftop Restaurant, Heritage Courtyard, Free WiFi, Airport Shuttle, Folk Performances",
                new BigDecimal("4500.00"),
                3
            },
            {
                4L,
                "Snow Peak Alpine Retreat",
                "Manali",
                "A cozy cedar wood sanctuary tucked away in Himachal pines, featuring roaring fireplaces, mountain stream views, and balconies overlooking snow peaks.",
                "Panoramic Mountain View, Fireplace Lounge, Heated Rooms, Free WiFi, Bonfire & Barbecue, Trekking Guide, Cafe",
                new BigDecimal("3800.00"),
                4
            },
            {
                5L,
                "Marine Bay Boulevard Suites",
                "Mumbai",
                "An art deco luxury hotel overlooking Marine Drive and the Arabian Sea, placing guests in the vibrant cultural and financial heart of South Mumbai.",
                "Arabian Sea View, 24/7 In-Room Dining, Modern Fitness Center, Business Lounge, High-Speed WiFi, Rooftop Cafe",
                new BigDecimal("6800.00"),
                5
            },
            {
                6L,
                "TechCity Silicon Boutique Hotel",
                "Bengaluru",
                "A contemporary eco-luxe hotel located in Indiranagar, featuring high-speed connectivity, ergonomic workspaces, artisan coffee bars, and lush terraces.",
                "Gigabit WiFi, Ergonomic Workspaces, Specialty Coffee Bar, Lush Garden Terrace, EV Charging Station, Fitness Studio",
                new BigDecimal("4200.00"),
                3
            }
        };

        for (Object[] hotel : sampleHotels) {
            jdbcTemplate.update(insertSql, hotel);
        }
    }
}
