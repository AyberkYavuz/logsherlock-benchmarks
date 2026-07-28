import { Logger } from "pino";
import { hotels } from "../data/hotels";
import { Hotel } from "../models/hotel";
import { scenarioManager } from "../scenario/scenario-manager";
import { Scenario } from "../scenario/scenario";


class HotelService {
  public listHotels(requestLogger: Logger): ReadonlyArray<Hotel> {
    const hotelLogger = requestLogger.child({service: "hotel"});
    hotelLogger.info(
      {
        event: "hotel_catalog_requested",
        hotelCount: hotels.length,
      },
      "Returning hotel catalog",
    );

    return hotels;
  }

  public checkAvailability(requestLogger: Logger, hotelId: string): Hotel {
    const hotelLogger = requestLogger.child({service:"hotel"});
    hotelLogger.info(
      {
        event: "availability_check_started",
        hotelId,
      },
      "Checking hotel availability",
    );

    const hotel = hotels.find((hotel) => hotel.id === hotelId);

    if (!hotel) {
      hotelLogger.error(
        {
          event: "hotel_not_found",
          hotelId,
        },
        "Hotel not found",
      );

      throw new Error(`Unknown hotel: ${hotelId}`);
    }

    if (scenarioManager.get() === Scenario.NO_ROOMS_AVAILABLE) {
      hotelLogger.warn(
        {
          event: "no_rooms_available",
          hotelId,
        },
        "No rooms available",
      );

      throw new Error("No rooms available");
    }

    hotelLogger.info(
      {
        event: "availability_confirmed",
        hotelId,
        availableRooms: hotel.availableRooms,
      },
      "Hotel availability confirmed",
    );

    return hotel;
  }
}

export const hotelService = new HotelService();
