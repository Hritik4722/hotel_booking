-- Spring Boot initial seed data for 6 Indian hotels
INSERT INTO hotels (id, name, location, description, amenities, price_per_night, total_rooms)
VALUES
(
    1,
    'The Grand Heritage Palace',
    'Udaipur',
    'A majestic royal retreat nestled on the tranquil banks of Lake Pichola with panoramic views of the Aravalli hills and authentic Mewar architecture.',
    'Free High-Speed WiFi, Infinity Lake-View Pool, Royal Spa, Fine Dining Restaurant, Valet Parking, Cocktail Lounge',
    7500.00,
    4
),
(
    2,
    'Taj Vista Coastal Resort',
    'Goa',
    'A luxury beachfront haven surrounded by swaying palms and golden sands, offering seaside cabanas, water sports, and sunset cocktail lounges.',
    'Direct Beach Access, Oceanfront Pool, Ayurvedic Spa, Seafood Bistro, Free WiFi, Yoga Deck, Beach Bar',
    5200.00,
    5
),
(
    3,
    'The Royal Amber Haveli',
    'Jaipur',
    'An authentic Rajasthani heritage mansion located in the Pink City featuring traditional courtyards, hand-painted frescoes, and evening cultural shows.',
    'Complimentary Royal Breakfast, Rooftop Restaurant, Heritage Courtyard, Free WiFi, Airport Shuttle, Folk Performances',
    4500.00,
    3
),
(
    4,
    'Snow Peak Alpine Retreat',
    'Manali',
    'A cozy cedar wood sanctuary tucked away in Himachal pines, featuring roaring fireplaces, mountain stream views, and balconies overlooking snow peaks.',
    'Panoramic Mountain View, Fireplace Lounge, Heated Rooms, Free WiFi, Bonfire & Barbecue, Trekking Guide, Cafe',
    3800.00,
    4
),
(
    5,
    'Marine Bay Boulevard Suites',
    'Mumbai',
    'An art deco luxury hotel overlooking Marine Drive and the Arabian Sea, placing guests in the vibrant cultural and financial heart of South Mumbai.',
    'Arabian Sea View, 24/7 In-Room Dining, Modern Fitness Center, Business Lounge, High-Speed WiFi, Rooftop Cafe',
    6800.00,
    5
),
(
    6,
    'TechCity Silicon Boutique Hotel',
    'Bengaluru',
    'A contemporary eco-luxe hotel located in Indiranagar, featuring high-speed connectivity, ergonomic workspaces, artisan coffee bars, and lush terraces.',
    'Gigabit WiFi, Ergonomic Workspaces, Specialty Coffee Bar, Lush Garden Terrace, EV Charging Station, Fitness Studio',
    4200.00,
    3
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    location = VALUES(location),
    description = VALUES(description),
    amenities = VALUES(amenities),
    price_per_night = VALUES(price_per_night),
    total_rooms = VALUES(total_rooms);
