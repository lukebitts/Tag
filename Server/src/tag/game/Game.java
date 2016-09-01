package tag.game;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;

public class Game extends Thread {
	
	private DiscreteDynamicsWorld world;
	private Room room;
	
	public Game(Room room) {
		final Vector3f worldMin = new Vector3f(-50f, 0f, -50f);
		final Vector3f worldMax = new Vector3f(50f, 100f, 50f);
		AxisSweep3 sweepBP = new AxisSweep3(worldMin, worldMax);
		
		sweepBP.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		
		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		world = new DiscreteDynamicsWorld(dispatcher, sweepBP, solver, collisionConfiguration);
		this.room = room;
	}
	
	public synchronized void addUser(User user) {
		
	}
	
	public synchronized void removeUser(User user) {
		
	}
	
	@Override
	public void run() {
		final double TEN_TO_NINTH = Math.pow(10, 9);
		final double ONE_OVER_FPS = 1.0 / 60.0;
		final double ONE_OVER_PHYSICS_FPS = 1.0 / 60.0;
		double accumulator = 0;
		double network_accumulator = 0;
		double lastTime = System.nanoTime() / TEN_TO_NINTH;
		
		while (!Thread.interrupted()) {
			
			if (accumulator >= ONE_OVER_FPS) {
				world.stepSimulation((float)ONE_OVER_FPS, 10);
				accumulator -= ONE_OVER_FPS;
			}

			if(network_accumulator >= ONE_OVER_PHYSICS_FPS) {
				/*for(Player player : players) {
					player.updateMmoPosition();
				}*/
				network_accumulator -= ONE_OVER_PHYSICS_FPS;
			}
			
			double currentTime = System.nanoTime() / TEN_TO_NINTH;
			double delta = currentTime - lastTime;

			accumulator += delta;
			network_accumulator += delta;
			lastTime = currentTime;
		}
	}
}
