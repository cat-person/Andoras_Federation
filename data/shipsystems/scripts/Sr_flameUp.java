package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class Sr_flameUp extends BaseShipSystemScript {


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (effectLevel > 0){
            if (index == 0) {
                return new StatusData("Flamer Empowered", false);
            }
        }
        return null;
    }

}
