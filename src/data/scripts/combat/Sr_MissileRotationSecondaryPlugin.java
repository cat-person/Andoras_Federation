package data.scripts.combat;

import com.fs.starfarer.api.combat.*;


public class Sr_MissileRotationSecondaryPlugin implements OnFireEffectPlugin {

        private static final String fragWeaponId = "sr_karelipod_dummy1";	//Id of the FRAG projectile version of the weapon
	private static final String energyWeaponId = "sr_karelipod"; //Id of the ENERGY projectile version of the weapon
	private int fireCounter = 0;
	
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = weapon.getShip();
		
		if (fireCounter >= 2 && fireCounter < 3) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, fragWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}		
		} else if (fireCounter >= 3 && fireCounter < 4) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, energyWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}			
		} else if (fireCounter >= 4) {
			if (engine.isEntityInPlay(projectile) && !projectile.didDamage()) {
				DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, fragWeaponId, projectile.getLocation(), projectile.getFacing(), ship.getVelocity());
				//newProj.setDamageAmount(projectile.getDamageAmount());
				engine.removeEntity(projectile);
				fireCounter++;
			}
			if (fireCounter >= 6) {
				fireCounter = 0;
			}
		} else fireCounter++;
	}
	
	
}




