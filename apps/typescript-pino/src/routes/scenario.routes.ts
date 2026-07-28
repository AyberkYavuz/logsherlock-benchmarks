import { Router } from "express";

import { ScenarioRequest } from "../models/scenario-request";
import { Scenario } from "../scenario/scenario";
import { scenarioManager } from "../scenario/scenario-manager";

export const scenarioRouter = Router();

scenarioRouter.post("/", (req, res) => {
  const body = req.body as ScenarioRequest;

  const scenario = Object.values(Scenario).find(
    (value) => value === body.scenario,
  );

  if (!scenario) {
    req.logger.warn(
      {
        event: "invalid_scenario",
        requestedScenario: body.scenario,
      },
      "Unknown scenario requested",
    );

    return res.status(400).json({
      error: "Unknown scenario",
    });
  }

  const previous = scenarioManager.get();

  scenarioManager.set(scenario);

  req.logger.warn(
    {
      event: "scenario_changed",
      previousScenario: previous,
      currentScenario: scenario,
    },
    "Scenario changed",
  );

  return res.json({
    status: "success",
    scenario,
  });
});
