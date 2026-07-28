export interface Booking {
  bookingId: string;
  customerId: string;
  hotelId: string;
  nights: number;
  status: "confirmed" | "cancelled";
}
