export interface Booking {
  bookingId: string;
  hotelId: string;
  guestName: string;
  nights: number;
  status: "confirmed" | "cancelled";
}
