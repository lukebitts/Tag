package tag.game;

import java.util.ArrayList;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;

public class Game extends Thread {
	
	private DiscreteDynamicsWorld world;
	private ArrayList<Player> players = new ArrayList<Player>();
	
	public Game(Room room) {
		final Vector3f worldMin = new Vector3f(-50f, 0f, -50f);
		final Vector3f worldMax = new Vector3f(50f, 100f, 50f);
		AxisSweep3 sweepBP = new AxisSweep3(worldMin, worldMax);
		
		sweepBP.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		
		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		world = new DiscreteDynamicsWorld(dispatcher, sweepBP, solver, collisionConfiguration);
		
		spawnFloor();
		
		for(User u : room.getUserList()) {
			spawnPlayer(u);
		}
	}
	
	@Override
	public void run() {
		final double TEN_TO_NINTH = Math.pow(10, 9);
		final double ONE_OVER_FPS = 1.0 / 60.0;
		final double ONE_OVER_PHYSICS_FPS = 1.0 / 10.0;
		double accumulator = 0;
		double network_accumulator = 0;
		double lastTime = System.nanoTime() / TEN_TO_NINTH;;
		
		while (!Thread.interrupted()) {
			
			if (accumulator >= ONE_OVER_FPS) {
				world.stepSimulation((float)ONE_OVER_FPS, 10);
				accumulator -= ONE_OVER_FPS;
			}

			if(network_accumulator >= ONE_OVER_PHYSICS_FPS) {
				for(Player player : players) {
					player.updatePositionVariables();
				}
				network_accumulator -= ONE_OVER_PHYSICS_FPS;
			}
			
			double currentTime = System.nanoTime() / TEN_TO_NINTH;
			double delta = currentTime - lastTime;

			accumulator += delta;
			network_accumulator += delta;
			lastTime = currentTime;
			
			for(Player player : players) {
				player.updateInput(world, (float)delta);
			}
		}
	}
	
	private void spawnFloor() {
		CollisionShape groundShape = new BoxShape(new Vector3f(50, 3, 50));//new StaticPlaneShape(new Vector3f(0, 1, 0), 1);
		Transform groundTransform = new Transform();
		groundTransform.setRotation(new Quat4f(0.f, 0.f, 0.f, 1.f));
		groundTransform.origin.set(new float[] { 0, -1, 0 });
		DefaultMotionState groundMotionState = new DefaultMotionState(groundTransform);

		RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0, 0, 0));
		groundRigidBodyCI.restitution = 0.5f;
		RigidBody groundRigidBody = new RigidBody(groundRigidBodyCI);

		world.addRigidBody(groundRigidBody);
	}
	
	private void spawnPlayer(User u) {
		players.add(new Player(u, world, new Vector3f(0,0,0)));
	}
}
