package com.hotelbooking.config;

import com.hotelbooking.entity.Hotel;
import com.hotelbooking.repository.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Initializes realistic sample hotel data on application startup if none exists.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final HotelRepository hotelRepository;

    public DataInitializer(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        long count = hotelRepository.count();
        if (count == 0) {
            log.info("No hotels found in database. Seeding 6 realistic sample Indian hotels...");
            List<Hotel> seedHotels = List.of(
                Hotel.builder()
                    .name("The Grand Heritage Palace")
                    .location("Udaipur")
                    .description("A majestic royal retreat nestled on the tranquil banks of Lake Pichola with panoramic views of the Aravalli hills and authentic Mewar architecture.")
                    .amenities("Free High-Speed WiFi, Infinity Lake-View Pool, Royal Spa, Fine Dining Restaurant, Valet Parking, Cocktail Lounge")
                    .pricePerNight(new BigDecimal("7500.00"))
                    .totalRooms(4)
                    .build(),
                Hotel.builder()
                    .name("Taj Vista Coastal Resort")
                    .location("Goa")
                    .description("A luxury beachfront haven surrounded by swaying palms and golden sands, offering seaside cabanas, water sports, and sunset cocktail lounges.")
                    .amenities("Direct Beach Access, Oceanfront Pool, Ayurvedic Spa, Seafood Bistro, Free WiFi, Yoga Deck, Beach Bar")
                    .pricePerNight(new BigDecimal("5200.00"))
                    .totalRooms(5)
                    .build(),
                Hotel.builder()
                    .name("The Royal Amber Haveli")
                    .location("Jaipur")
                    .description("An authentic Rajasthani heritage mansion located in the Pink City featuring traditional courtyards, hand-painted frescoes, and evening cultural shows.")
                    .amenities("Complimentary Royal Breakfast, Rooftop Restaurant, Heritage Courtyard, Free WiFi, Airport Shuttle, Folk Performances")
                    .pricePerNight(new BigDecimal("4500.00"))
                    .totalRooms(3)
                    .build(),
                Hotel.builder()
                    .name("Snow Peak Alpine Retreat")
                    .location("Manali")
                    .description("A cozy cedar wood sanctuary tucked away in Himachal pines, featuring roaring fireplaces, mountain stream views, and balconies overlooking snow peaks.")
                    .amenities("Panoramic Mountain View, Fireplace Lounge, Heated Rooms, Free WiFi, Bonfire & Barbecue, Trekking Guide, Cafe")
                    .pricePerNight(new BigDecimal("3800.00"))
                    .totalRooms(4)
                    .build(),
                Hotel.builder()
                    .name("Marine Bay Boulevard Suites")
                    .location("Mumbai")
                    .description("An art deco luxury hotel overlooking Marine Drive and the Arabian Sea, placing guests in the vibrant cultural and financial heart of South Mumbai.")
                    .amenities("Arabian Sea View, 24/7 In-Room Dining, Modern Fitness Center, Business Lounge, High-Speed WiFi, Rooftop Cafe")
                    .pricePerNight(new BigDecimal("6800.00"))
                    .totalRooms(5)
                    .build(),
                Hotel.builder()
                    .name("TechCity Silicon Boutique Hotel")
                    .location("Bengaluru")
                    .description("A contemporary eco-luxe hotel located in Indiranagar, featuring high-speed connectivity, ergonomic workspaces, artisan coffee bars, and lush terraces.")
                    .amenities("Gigabit WiFi, Ergonomic Workspaces, Specialty Coffee Bar, Lush Garden Terrace, EV Charging Station, Fitness Studio")
                    .pricePerNight(new BigDecimal("4200.00"))
                    .totalRooms(3)
                    .build()
            );

            hotelRepository.saveAll(seedHotels);
            log.info("Successfully seeded {} Indian hotel records via HotelRepository.", seedHotels.size());
        } else {
            log.info("Database already contains {} hotels. Skipping initial seeding.", count);
        }
    }
}
