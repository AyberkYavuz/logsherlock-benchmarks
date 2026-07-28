import { Scenario } from "./scenario";

class ScenarioManager {
  private currentScenario: Scenario = Scenario.NORMAL;

  public get(): Scenario {
    return this.currentScenario;
  }

  public set(scenario: Scenario): void {
    this.currentScenario = scenario;
  }
}

export const scenarioManager = new ScenarioManager();
