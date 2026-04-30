package com.epam.edp.demo.config;

import com.epam.edp.demo.model.Tour;
import com.epam.edp.demo.repository.TourRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(TourRepository tourRepository) {
        return args -> {
            if (tourRepository.count() == 0) {
                System.out.println("Initializing sample tour data...");

                List<Tour> tours = Arrays.asList(

                    // ── Punta Cana, Dominican Republic ── Resorts ──
                    createTour("tour-001", "Garden Resort & Spa",
                        "Punta Cana, Dominican Republic",
                        "Lush tropical gardens with a world-class spa and pristine beaches.",
                        5.0, 19,
                        Arrays.asList(LocalDate.of(2025, 1, 4), LocalDate.of(2025, 2, 8), LocalDate.of(2025, 3, 15)),
                        Arrays.asList("7 days", "10 days", "12 days"),
                        Map.of("7 days", "1400", "10 days", "1900", "12 days", "2200"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 80, 42, 1),

                    createTour("tour-002", "Tropical Caribe",
                        "Punta Cana, Dominican Republic",
                        "All-inclusive Caribbean paradise with water sports and nightlife.",
                        5.0, 19,
                        Arrays.asList(LocalDate.of(2025, 1, 4), LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 22)),
                        Arrays.asList("7 days", "10 days", "12 days"),
                        Map.of("7 days", "1400", "10 days", "1850", "12 days", "2100"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 100, 55, 5),

                    createTour("tour-003", "Ocean Blue Resort",
                        "Punta Cana, Dominican Republic",
                        "Beachfront luxury with infinity pools and gourmet dining.",
                        4.8, 34,
                        Arrays.asList(LocalDate.of(2025, 1, 11), LocalDate.of(2025, 2, 15)),
                        Arrays.asList("7 days", "10 days", "14 days"),
                        Map.of("7 days", "1600", "10 days", "2200", "14 days", "2800"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 60, 30, 7),

                    createTour("tour-004", "Coral Sands Villa",
                        "Punta Cana, Dominican Republic",
                        "Exclusive boutique resort with private beach and butler service.",
                        4.9, 12,
                        Arrays.asList(LocalDate.of(2025, 1, 18), LocalDate.of(2025, 3, 1)),
                        Arrays.asList("5 days", "7 days", "10 days"),
                        Map.of("5 days", "1200", "7 days", "1650", "10 days", "2300"),
                        Arrays.asList("BB", "HB", "AI"),
                        "Resorts", 30, 18, 3),

                    // ── Punta Cana ── Cruises ──
                    createTour("tour-005", "Caribbean Cruise Explorer",
                        "Punta Cana, Dominican Republic",
                        "7-day cruise through the Caribbean islands departing from Punta Cana.",
                        4.7, 45,
                        Arrays.asList(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 2, 10)),
                        Arrays.asList("7 days", "10 days"),
                        Map.of("7 days", "2100", "10 days", "2800"),
                        Arrays.asList("FB", "AI"),
                        "Cruises", 200, 155, 14),

                    createTour("tour-006", "Island Hopper Cruise",
                        "Punta Cana, Dominican Republic",
                        "Hop between Dominican islands on a luxury catamaran.",
                        4.6, 28,
                        Arrays.asList(LocalDate.of(2025, 1, 20), LocalDate.of(2025, 3, 5)),
                        Arrays.asList("5 days", "7 days"),
                        Map.of("5 days", "1500", "7 days", "2000"),
                        Arrays.asList("HB", "FB", "AI"),
                        "Cruises", 50, 32, 10),

                    // ── Punta Cana ── Hikes ──
                    createTour("tour-007", "Rainforest Trek Adventure",
                        "Punta Cana, Dominican Republic",
                        "Guided hike through tropical rainforests and hidden waterfalls.",
                        4.4, 22,
                        Arrays.asList(LocalDate.of(2025, 1, 8), LocalDate.of(2025, 2, 12)),
                        Arrays.asList("3 days", "5 days"),
                        Map.of("3 days", "650", "5 days", "950"),
                        Arrays.asList("BB", "HB"),
                        "Hikes", 20, 8, 2),

                    // ── Paris, France ──
                    createTour("tour-008", "Romantic Escapade to Paris",
                        "Paris, France",
                        "Experience the city of love with Eiffel Tower views and fine dining.",
                        4.8, 245,
                        Arrays.asList(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 14), LocalDate.of(2025, 3, 20)),
                        Arrays.asList("7 days", "10 days"),
                        Map.of("7 days", "2500", "10 days", "3200"),
                        Arrays.asList("BB", "HB", "FB"),
                        "Resorts", 50, 23, 14),

                    createTour("tour-009", "Parisian Art & Culture",
                        "Paris, France",
                        "Explore world-famous museums, galleries, and historic cafés.",
                        4.6, 189,
                        Arrays.asList(LocalDate.of(2025, 1, 20), LocalDate.of(2025, 2, 25)),
                        Arrays.asList("5 days", "7 days"),
                        Map.of("5 days", "1800", "7 days", "2400"),
                        Arrays.asList("BB", "HB"),
                        "Hikes", 40, 22, 7),

                    // ── Zurich, Switzerland ──
                    createTour("tour-010", "Alpine Adventure in Switzerland",
                        "Zurich, Switzerland",
                        "Breathtaking journey through the Swiss Alps with glacier views.",
                        4.9, 189,
                        Arrays.asList(LocalDate.of(2025, 1, 20), LocalDate.of(2025, 3, 10)),
                        Arrays.asList("7 days", "10 days", "12 days"),
                        Map.of("7 days", "3000", "10 days", "4200", "12 days", "4800"),
                        Arrays.asList("BB", "HB"),
                        "Hikes", 30, 12, 21),

                    createTour("tour-011", "Swiss Lakeside Retreat",
                        "Zurich, Switzerland",
                        "Relax beside crystal-clear alpine lakes in luxury chalets.",
                        4.7, 98,
                        Arrays.asList(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 3, 15)),
                        Arrays.asList("7 days", "10 days"),
                        Map.of("7 days", "2800", "10 days", "3800"),
                        Arrays.asList("BB", "HB", "FB"),
                        "Resorts", 25, 15, 10),

                    // ── Maldives ──
                    createTour("tour-012", "Beach Paradise in Maldives",
                        "Maldives",
                        "Overwater bungalows and crystal-clear lagoons.",
                        4.7, 312,
                        Arrays.asList(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 2, 5)),
                        Arrays.asList("7 days", "10 days", "14 days"),
                        Map.of("7 days", "4500", "10 days", "6000", "14 days", "7500"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 40, 35, 30),

                    createTour("tour-013", "Maldives Diving Expedition",
                        "Maldives",
                        "Explore vibrant coral reefs and swim with manta rays.",
                        4.9, 67,
                        Arrays.asList(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 20)),
                        Arrays.asList("5 days", "7 days", "10 days"),
                        Map.of("5 days", "3200", "7 days", "4200", "10 days", "5500"),
                        Arrays.asList("HB", "FB", "AI"),
                        "Cruises", 24, 16, 5),

                    // ── Rome, Italy ──
                    createTour("tour-014", "Historic Rome Experience",
                        "Rome, Italy",
                        "Walk through the Colosseum, Vatican, and ancient forums.",
                        4.6, 421,
                        Arrays.asList(LocalDate.of(2025, 1, 12), LocalDate.of(2025, 2, 28)),
                        Arrays.asList("5 days", "7 days"),
                        Map.of("5 days", "1800", "7 days", "2400"),
                        Arrays.asList("BB", "HB", "FB"),
                        "Hikes", 60, 28, 10),

                    createTour("tour-015", "Amalfi Coast & Rome Luxury",
                        "Rome, Italy",
                        "Combine Rome sightseeing with Amalfi Coast beach luxury.",
                        4.8, 156,
                        Arrays.asList(LocalDate.of(2025, 1, 18), LocalDate.of(2025, 3, 8)),
                        Arrays.asList("10 days", "14 days"),
                        Map.of("10 days", "3500", "14 days", "4800"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 35, 20, 14),

                    // ── Cancun, Mexico ──
                    createTour("tour-016", "Cancun All-Inclusive Escape",
                        "Cancun, Mexico",
                        "Turquoise waters, Mayan ruins, and all-inclusive luxury.",
                        4.5, 278,
                        Arrays.asList(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 2, 9), LocalDate.of(2025, 3, 16)),
                        Arrays.asList("7 days", "10 days", "14 days"),
                        Map.of("7 days", "1600", "10 days", "2200", "14 days", "2900"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 120, 78, 7),

                    createTour("tour-017", "Yucatan Explorer Hike",
                        "Cancun, Mexico",
                        "Trek through cenotes, jungles, and ancient Mayan temples.",
                        4.3, 95,
                        Arrays.asList(LocalDate.of(2025, 1, 14), LocalDate.of(2025, 2, 18)),
                        Arrays.asList("5 days", "7 days"),
                        Map.of("5 days", "900", "7 days", "1250"),
                        Arrays.asList("BB", "HB"),
                        "Hikes", 25, 10, 3),

                    // ── Bali, Indonesia ──
                    createTour("tour-018", "Bali Serenity Resort",
                        "Bali, Indonesia",
                        "Temple visits, rice terraces, and oceanfront spa experiences.",
                        4.8, 203,
                        Arrays.asList(LocalDate.of(2025, 1, 7), LocalDate.of(2025, 2, 11)),
                        Arrays.asList("7 days", "10 days", "14 days"),
                        Map.of("7 days", "1800", "10 days", "2500", "14 days", "3200"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 50, 38, 10),

                    createTour("tour-019", "Bali Volcano Trek",
                        "Bali, Indonesia",
                        "Sunrise trek to Mount Batur and explore hidden waterfalls.",
                        4.5, 134,
                        Arrays.asList(LocalDate.of(2025, 1, 12), LocalDate.of(2025, 2, 22)),
                        Arrays.asList("3 days", "5 days"),
                        Map.of("3 days", "550", "5 days", "850"),
                        Arrays.asList("BB", "HB"),
                        "Hikes", 18, 7, 2),

                    // ── Santorini, Greece ──
                    createTour("tour-020", "Santorini Sunset Resort",
                        "Santorini, Greece",
                        "Whitewashed villas, caldera views, and Mediterranean cuisine.",
                        4.9, 167,
                        Arrays.asList(LocalDate.of(2025, 1, 20), LocalDate.of(2025, 3, 1)),
                        Arrays.asList("7 days", "10 days"),
                        Map.of("7 days", "2200", "10 days", "3000"),
                        Arrays.asList("BB", "HB", "FB"),
                        "Resorts", 28, 22, 7),

                    createTour("tour-021", "Greek Islands Cruise",
                        "Santorini, Greece",
                        "Sail between Santorini, Mykonos, Crete, and Rhodes.",
                        4.7, 89,
                        Arrays.asList(LocalDate.of(2025, 1, 25), LocalDate.of(2025, 3, 10)),
                        Arrays.asList("7 days", "10 days", "14 days"),
                        Map.of("7 days", "2600", "10 days", "3500", "14 days", "4500"),
                        Arrays.asList("FB", "AI"),
                        "Cruises", 150, 120, 14),

                    // ── Tokyo, Japan ──
                    createTour("tour-022", "Tokyo Cultural Discovery",
                        "Tokyo, Japan",
                        "Temples, cherry blossoms, and authentic ramen experiences.",
                        4.6, 312,
                        Arrays.asList(LocalDate.of(2025, 1, 8), LocalDate.of(2025, 2, 14)),
                        Arrays.asList("7 days", "10 days"),
                        Map.of("7 days", "2800", "10 days", "3800"),
                        Arrays.asList("BB", "HB"),
                        "Hikes", 40, 25, 10),

                    // ── Dubai, UAE ──
                    createTour("tour-023", "Dubai Luxury Experience",
                        "Dubai, UAE",
                        "Desert safaris, skyscraper views, and gold-souk shopping.",
                        4.7, 256,
                        Arrays.asList(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 2, 7)),
                        Arrays.asList("5 days", "7 days", "10 days"),
                        Map.of("5 days", "2000", "7 days", "2800", "10 days", "3600"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 90, 60, 5),

                    // ── Barcelona, Spain ──
                    createTour("tour-024", "Barcelona Beach & Culture",
                        "Barcelona, Spain",
                        "Gaudí architecture, tapas tours, and Mediterranean beaches.",
                        4.5, 198,
                        Arrays.asList(LocalDate.of(2025, 1, 16), LocalDate.of(2025, 2, 20)),
                        Arrays.asList("5 days", "7 days"),
                        Map.of("5 days", "1500", "7 days", "2100"),
                        Arrays.asList("BB", "HB", "FB"),
                        "Resorts", 55, 30, 7),

                    // ── Maui, Hawaii ──
                    createTour("tour-025", "Maui Paradise Retreat",
                        "Maui, Hawaii",
                        "Volcanic landscapes, whale watching, and luau celebrations.",
                        4.8, 178,
                        Arrays.asList(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 2, 15)),
                        Arrays.asList("7 days", "10 days", "14 days"),
                        Map.of("7 days", "2400", "10 days", "3200", "14 days", "4100"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 45, 28, 10),

                    // ── Pune, India ──
                    createTour("tour-026", "Pune Heritage Trail",
                        "Pune, India",
                        "Explore forts, temples, and Maharashtra's rich history.",
                        4.5, 156,
                        Arrays.asList(LocalDate.of(2025, 1, 8), LocalDate.of(2025, 2, 22)),
                        Arrays.asList("3 days", "5 days", "7 days"),
                        Map.of("3 days", "500", "5 days", "750", "7 days", "1000"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Hikes", 100, 45, 7),

                    // ── Costa Rica ──
                    createTour("tour-027", "Costa Rica Rainforest Lodge",
                        "San José, Costa Rica",
                        "Zip-lining, hot springs, and wildlife in cloud forests.",
                        4.6, 142,
                        Arrays.asList(LocalDate.of(2025, 1, 12), LocalDate.of(2025, 2, 16)),
                        Arrays.asList("7 days", "10 days"),
                        Map.of("7 days", "1900", "10 days", "2600"),
                        Arrays.asList("BB", "HB", "FB"),
                        "Hikes", 35, 20, 5),

                    // ── Norwegian Fjords ──
                    createTour("tour-028", "Norwegian Fjord Cruise",
                        "Bergen, Norway",
                        "Sail through majestic fjords with Northern Lights viewing.",
                        4.9, 76,
                        Arrays.asList(LocalDate.of(2025, 1, 18), LocalDate.of(2025, 3, 5)),
                        Arrays.asList("10 days", "14 days"),
                        Map.of("10 days", "4000", "14 days", "5500"),
                        Arrays.asList("FB", "AI"),
                        "Cruises", 180, 140, 21),

                    // ── Thailand ──
                    createTour("tour-029", "Phuket Beach Resort",
                        "Phuket, Thailand",
                        "White sand beaches, floating markets, and Thai massage retreats.",
                        4.4, 225,
                        Arrays.asList(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 2, 10)),
                        Arrays.asList("7 days", "10 days", "14 days"),
                        Map.of("7 days", "1300", "10 days", "1800", "14 days", "2300"),
                        Arrays.asList("BB", "HB", "FB", "AI"),
                        "Resorts", 70, 50, 5),

                    createTour("tour-030", "Thai Island Hopper",
                        "Phuket, Thailand",
                        "Hop between Phi Phi, Krabi, and Koh Lanta by speedboat.",
                        4.6, 88,
                        Arrays.asList(LocalDate.of(2025, 1, 14), LocalDate.of(2025, 2, 18)),
                        Arrays.asList("7 days", "10 days"),
                        Map.of("7 days", "1700", "10 days", "2300"),
                        Arrays.asList("HB", "FB"),
                        "Cruises", 30, 22, 7)
                );

                tourRepository.saveAll(tours);
                System.out.println("Initialized " + tours.size() + " sample tours!");
            }
        };
    }

    private Tour createTour(String id, String name, String destination, String summary,
                           Double rating, Integer reviewCount, List<LocalDate> startDates,
                           List<String> durations, Map<String, String> pricePerDuration,
                           List<String> mealPlans, String tourType,
                           Integer totalCapacity, Integer bookedCount, Integer freeCancellationDays) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setName(name);
        tour.setDestination(destination);
        tour.setSummary(summary);
        tour.setRating(rating);
        tour.setReviewCount(reviewCount);
        tour.setStartDates(startDates);
        tour.setDurations(durations);
        tour.setPricePerDuration(pricePerDuration);
        tour.setMealPlans(mealPlans);
        tour.setTourType(tourType);
        tour.setTotalCapacity(totalCapacity);
        tour.setBookedCount(bookedCount);
        tour.setFreeCancellationDaysBefore(freeCancellationDays);

        Tour.GuestQuantity guestQuantity = new Tour.GuestQuantity();
        guestQuantity.setAdultsMaxValue(4);
        guestQuantity.setChildrenMaxValue(2);
        guestQuantity.setTotalMaxValue(6);
        tour.setGuestQuantity(guestQuantity);

        return tour;
    }
}

