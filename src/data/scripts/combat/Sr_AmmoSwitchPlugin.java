package src.data.scripts.combat;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;


public class Sr_AmmoSwitchPlugin implements OnFireEffectPlugin {

	private static final String explosiveWeaponId = "sr_livyatan_dummy";	//Id of the HE projectile version of the weapon
	private int fireCounter = 0;
	
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		
		if (fireCounter >= 2) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, explosiveWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
			}			
			fireCounter = 0;
		} else fireCounter++;
	}
	
	
}




