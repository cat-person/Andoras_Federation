package src.data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;

public class WingNoLanding extends BaseHullMod {

	public void advanceInCombat(ShipAPI ship, float amount) {
		
      if (ship.isAlive()) {
         ShipAPI carrier = ship.getWing().getSourceShip();
         if (carrier != null && (carrier.isRetreating() || !carrier.isAlive()) && !ship.isRetreating()) {
            ship.setRetreating(true, true);
         } else {
            if (ship.isRetreating()) {
               return;
            }

            ship.abortLanding();
            if (ship.isLanding()) {
               ship.getWing().stopReturning(ship);			  
               ship.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);		   
            }
         }
      }

   }


}
