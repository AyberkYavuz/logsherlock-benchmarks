import { Hotel } from "../models/hotel";

export const hotels: ReadonlyArray<Hotel> = [
  {
    id: "HTL-001",
    name: "Grand Istanbul Hotel",
    city: "Istanbul",
    availableRooms: 15,
  },
  {
    id: "HTL-002",
    name: "Blue Bosphorus Suites",
    city: "Istanbul",
    availableRooms: 8,
  },
  {
    id: "HTL-003",
    name: "Cappadocia Cave Resort",
    city: "Nevşehir",
    availableRooms: 12,
  },
  {
    id: "HTL-004",
    name: "Antalya Beach Resort",
    city: "Antalya",
    availableRooms: 20,
  },
  {
    id: "HTL-005",
    name: "Pamukkale Thermal Hotel",
    city: "Denizli",
    availableRooms: 10,
  },
];
