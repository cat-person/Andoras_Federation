package data.scripts.weapons;
import com.fs.starfarer.api.combat.*;

public class Sr_EngineRotationPlugin implements EveryFrameWeaponEffectPlugin {
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        float angularVelocity = ship.getAngularVelocity() / ship.getMaxTurnRate();
        float angle = angularVelocity * 90f;
        for (ShipEngineControllerAPI.ShipEngineAPI shipEngine : ship.getEngineController().getShipEngines()) {
            shipEngine.getEngineSlot().setAngle(180f - angle);
        }
        weapon.setCurrAngle(ship.getFacing() - angle);
    }
}