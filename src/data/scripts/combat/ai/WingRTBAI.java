package src.data.scripts.combat.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

//import org.apache.log4j.Logger;

public class WingRTBAI implements ShipAIPlugin {
   private ShipAPI ship;
   private WeaponAPI dock;
   private CombatEngineAPI engine;
   private ShipwideAIFlags flags;
   private ShipAIConfig config;
   private Boolean lockedOnAngle = false;
   
   //public Logger log = Logger.getLogger(this.getClass());

   public WingRTBAI(ShipAPI _ship, WeaponAPI _dock, ShipwideAIFlags _flags, CombatEngineAPI _engine, ShipAIConfig _config) {
      this.ship = _ship;
      this.dock = _dock;
      this.flags = _flags;
      this.engine = _engine;
      this.config = _config;
   }

   public void setDoNotFireDelay(float amount) {
   }

   public void forceCircumstanceEvaluation() {
   }

   public void advance(float amount) {
      float distance = MathUtils.getDistance(this.ship.getLocation(), this.dock.getLocation());
      boolean docked = distance <= 60.0F;
      if (!docked) {
         float facing = this.ship.getFacing();
         facing = MathUtils.getShortestRotation(facing, VectorUtils.getAngle(this.ship.getLocation(), this.dock.getLocation()));
         float _distance = distance;
         if (distance > 1000.0F) {
            _distance = 1000.0F;
         }

         if (distance < 100.0F) {
            _distance = 100.0F;
         }

         float multiplier = (1450.0F - _distance) / 450.0F;
         float turnrate = this.ship.getMaxTurnRate() * multiplier;
		 if (Misc.getAngleDiff(this.ship.getFacing(), Misc.getAngleInDegrees(this.ship.getLocation(), this.dock.getLocation())) < 5f) {
			 if (!lockedOnAngle) {
				 this.ship.setAngularVelocity(0f);
				 //this.ship.getVelocity().set(0f, 0f);
				 lockedOnAngle = true;
			 }
			 this.ship.setFacing(Misc.getAngleInDegrees(this.ship.getLocation(), this.dock.getLocation()));
		 } else if (lockedOnAngle && Misc.getAngleDiff(this.ship.getFacing(), Misc.getAngleInDegrees(this.ship.getLocation(), this.dock.getLocation())) > 15f) {
			 lockedOnAngle = false;
		 } else if (!lockedOnAngle) {
			 this.ship.setAngularVelocity(Math.min(turnrate, Math.max(-turnrate, facing * 5.0F)));			 
		 }
         this.ship.giveCommand(ShipCommand.ACCELERATE, (Object)null, 0);
		 //log.info(Misc.getAngleDiff(this.ship.getFacing(), Misc.getAngleInDegrees(this.ship.getLocation(), this.dock.getLocation())) + "angleDIff");	 
		 //log.info(this.ship.getAngularVelocity() + "turnrate");
      }
   }

   public boolean needsRefit() {
      return false;
   }

   public ShipwideAIFlags getAIFlags() {
      return this.flags;
   }

   public void cancelCurrentManeuver() {
   }

   public ShipAIConfig getConfig() {
      return this.config;
   }
}