package theorb;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;

public class RigidBodyControlZLock extends RigidBodyControl implements PhysicsTickListener {

    private Vector3f linearVelocityVector;
    private Vector3f angularVelocityVector;

    public RigidBodyControlZLock(float mass) {
        super(mass);
    }

    @Override
    public void setPhysicsSpace(PhysicsSpace space) {
        super.setPhysicsSpace(space);
        space.addTickListener(this);
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float f) {
        linearVelocityVector = getLinearVelocity();
        angularVelocityVector = getAngularVelocity();
        linearVelocityVector.z = 0;
        angularVelocityVector.x = 0;
        angularVelocityVector.y = 0;
        setLinearVelocity(linearVelocityVector);
        setAngularVelocity(angularVelocityVector);
    }

    @Override
    public void physicsTick(PhysicsSpace space, float f) {
    }
}