package src.data.scripts.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;

import org.lwjgl.util.vector.Vector2f;

import src.data.scripts.combat.ai.WingRTBAI;

public class LandingPad implements EveryFrameWeaponEffectPlugin {
	
	//private final String hull_id = "uaf_wing_rafflesya"; // hull_id of the fighter
	private final String hull_id = "sr_thunderwing"; // hull_id of the fighter
	//private boolean isDocking = false;
	private boolean isInDockingProcedure = false;
	private IntervalUtil _rearmTimer = new IntervalUtil(19.0F, 21.0F);
	
	private ShipAIPlugin wingAIBackup = null;

	private boolean shouldRearm(ShipAPI ship) {
		List<WeaponAPI> weapons = ship.getAllWeapons();
		CombatEngineAPI eng = Global.getCombatEngine();
		int weaponCount = 0;
		int totalWeaponWithNoAmmunition = 0;
	  
		for (WeaponAPI weapon : weapons) {
			if (weapon.usesAmmo()) {
				weaponCount++;
				if (weapon.getAmmo() <= 10) {
					totalWeaponWithNoAmmunition++;
				}
			}
		}

		return weaponCount != 0 && weaponCount == totalWeaponWithNoAmmunition && ship.isAlive() && (ship.isFighter() || ship.isDrone()) && !ship.isHulk() && !ship.isShuttlePod() && eng.isEntityInPlay(ship) && !ship.isRetreating();
	}

	private void swapSprite(WeaponAPI weap, boolean dockStatus) {
	}

	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
	   
		Vector2f point = weapon.getLocation();
		ShipAPI parent = weapon.getShip();		
		List<FighterWingAPI> fighters = parent.getAllWings();
		Iterator var7 = fighters.iterator();
		
		for (FighterWingAPI fighter : fighters) {
			ShipAPI leader = null;
			if (fighter.getLeader() == null || !fighter.getLeader().isAlive() || !fighter.getLeader().getHullSpec().getHullId().equals(this.hull_id)) {
				continue;
			}
			leader = fighter.getLeader();
			if (this.wingAIBackup == null) {
				this.wingAIBackup = leader.getShipAI();
			}
			if (leader.isHulk()) {
				if (MathUtils.getDistance(leader, parent) <= parent.getCollisionRadius()) {
					leader.setCollisionClass(CollisionClass.NONE);
				} else {
					leader.setCollisionClass(CollisionClass.SHIP);
				}
			}
			
			List<WeaponAPI> weapons = leader.getAllWeapons();
			boolean isEngaging = !parent.isPullBackFighters();
            //boolean isReturning = parent.isPullBackFighters();
            boolean isRearming = this.shouldRearm(leader);
            //boolean isInDockingProcedure = leader.getCustomData().containsKey("WingAIBackup");
			
			if (isEngaging) {
               leader.getWing().stopReturning(leader);
            }
			
			if (isEngaging && !isRearming || !parent.isAlive()) {
				if (isInDockingProcedure) {
					leader.setShipAI(wingAIBackup);
					//for (WeaponGroupAPI group : leader.getWeaponGroupsCopy()) {
					//	group.toggleOn();
					//}
					this.isInDockingProcedure = false;
				}
				leader.getCustomData().remove(parent.toString() + "rearm_timer");
				//this.swapSprite(weapon, false);
            } else if (!MathUtils.isWithinRange(point, leader.getLocation(), 60.0F)) {
				if (!this.isInDockingProcedure) {
					ShipAIPlugin dockAI = new WingRTBAI(leader, weapon, leader.getAIFlags(), Global.getCombatEngine(), (ShipAIConfig) null);
					leader.setShipAI(dockAI);
					//for (WeaponGroupAPI group : leader.getWeaponGroupsCopy()) {
					//	group.toggleOff();
					//}
					this.isInDockingProcedure = true;
					//this.isDocking = true;				
				}			
            } else {
				fighter.stopReturning(leader);
				if (!this.isInDockingProcedure) {
					ShipAIPlugin dockAI = new WingRTBAI(leader, weapon, leader.getAIFlags(), Global.getCombatEngine(), (ShipAIConfig) null);
					leader.setShipAI(dockAI);
					//for (WeaponGroupAPI group : leader.getWeaponGroupsCopy()) {
					//	group.toggleOff();
					//}
					this.isInDockingProcedure = true;
				}
				Vector2f loc = leader.getLocation();
				loc.x = point.x;
				loc.y = point.y;
				if (leader.getFacing() == weapon.getCurrAngle()) {
					leader.setFacing(weapon.getCurrAngle());
				}

				float diff_mult = 1.0F;
				float mtr = leader.getMaxTurnRate() / 27.0F;
				float diff = MathUtils.getShortestRotation(leader.getFacing(), weapon.getCurrAngle());
				if (diff < 0.0F) {
					diff_mult = -1.0F;
				}
				float rotby = Math.min(Math.abs(diff), mtr);
				leader.setFacing(leader.getFacing() + rotby * diff_mult);
				if (!leader.getCustomData().containsKey(parent.toString() + "rearm_timer")) {
					leader.setCustomData(parent.toString() + "rearm_timer", new IntervalUtil(19.0F, 21.0F));
				}
				//this.swapSprite(weapon, true);
				
				if (isRearming) {
					IntervalUtil rearmTimer = (IntervalUtil)leader.getCustomData().get(parent.toString() + "rearm_timer");
					rearmTimer.advance(amount);
					if (rearmTimer.intervalElapsed()) {
						for (WeaponAPI _weap : weapons) {
							if (_weap.usesAmmo()) {
								_weap.setAmmo(_weap.getMaxAmmo());
							}
						}						 
					}
				}
				//this.isDocking = false;
            }
		}
   }
}