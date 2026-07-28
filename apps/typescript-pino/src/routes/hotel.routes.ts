import { Router } from "express";

import { hotelService } from "../services/hotel.service";

export const hotelRouter = Router();

hotelRouter.get("/", (req, res) => {
  const hotels = hotelService.listHotels(req.logger);

  res.json(hotels);
});
