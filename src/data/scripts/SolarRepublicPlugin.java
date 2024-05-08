package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.ai.RepairDrone_AI;

public class SolarRepublicPlugin extends BaseModPlugin {

    @Override
    public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
        if (ship.getHullSpec().getHullId().equals("sr_repairdrone")) {
            return new PluginPick(new RepairDrone_AI(ship), CampaignPlugin.PickPriority.MOD_GENERAL);
        }

        return super.pickShipAI(member, ship);
    }
}