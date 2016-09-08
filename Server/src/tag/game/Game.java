package tag.game;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.owens.oobjloader.builder.Build;
import com.owens.oobjloader.builder.Face;
import com.owens.oobjloader.builder.FaceVertex;
import com.owens.oobjloader.parser.Parse;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;

import tag.util.Util;
import todelete.GLShapeDrawer;
import todelete.GLDebugDrawer;
import todelete.IGL;
import todelete.LwjglGL;
import todelete.Camera;


public class Game extends Thread {
	
	static final boolean DEBUG_WORLD = false;
	
	private DiscreteDynamicsWorld world;
	private ArrayList<Player> players = new ArrayList<Player>();
	private Room room;
	
	//TODO: delete
	private IGL gl = new LwjglGL();
	
	public Game(Room room) {
		final Vector3f worldMin = new Vector3f(-50f, -10f, -50f);
		final Vector3f worldMax = new Vector3f(50f, 100f, 50f);
		AxisSweep3 sweepBP = new AxisSweep3(worldMin, worldMax);
		
		sweepBP.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		
		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		world = new DiscreteDynamicsWorld(dispatcher, sweepBP, solver, collisionConfiguration);
		this.room = room; 
		
		for(User u : room.getUserList()) {
			spawnPlayer(u);
		}
		Collections.shuffle(players);
		
		gameSetup();
	}
	
	private void gameSetup() {
		Player chased = players.remove(0);
		players.add(chased);
		chased.setBeingChased(true);
		
		final float HEIGHT_BUFFER = 5.f;
		
		for(Player p : players) {
			if(p.isBeingChased()) {
				p.setPosition(new Vector3f(0, 13 + HEIGHT_BUFFER, 0));
			}
			else {
				p.setPosition(new Vector3f(1.23f, 13 + HEIGHT_BUFFER, -1.36f));
			}
		}
		
		SFSObject message = new SFSObject();
		message.putInt("chased_id", chased.getSfsUser().getId());
		room.getExtension().send("game_start", message, room.getUserList());
	}

	//TODO: delete
	private void render(float delta) {
		if(!DEBUG_WORLD) return;
		
		world.getDebugDrawer().setDebugMode(
				  DebugDrawModes.DRAW_WIREFRAME | DebugDrawModes.DRAW_AABB 
				);
		world.debugDrawWorld();
		
		//---------------------------
		
		final Transform m = new Transform();
		Vector3f wireColor = new Vector3f();
		int numObjects = world.getNumCollisionObjects();
		wireColor.set(1f, 0f, 0f);
		for (int i = 0; i < numObjects; i++) {
			CollisionObject colObj = world.getCollisionObjectArray().getQuick(i);
			RigidBody body = RigidBody.upcast(colObj);

			if (body != null && body.getMotionState() != null) {
				DefaultMotionState myMotionState = (DefaultMotionState) body.getMotionState();
				m.set(myMotionState.graphicsWorldTrans);
			}
			else {
				colObj.getWorldTransform(m);
			}

			wireColor.set(1f, 1f, 0.5f); // wants deactivation
			if ((i & 1) != 0) {
				wireColor.set(0f, 0f, 1f);
			}

			// color differently for active, sleeping, wantsdeactivation states
			if (colObj.getActivationState() == 1) // active
			{
				if ((i & 1) != 0) {
					//wireColor.add(new Vector3f(1f, 0f, 0f));
					wireColor.x += 1f;
				}
				else {
					//wireColor.add(new Vector3f(0.5f, 0f, 0f));
					wireColor.x += 0.5f;
				}
			}
			if (colObj.getActivationState() == 2) // ISLAND_SLEEPING
			{
				if ((i & 1) != 0) {
					//wireColor.add(new Vector3f(0f, 1f, 0f));
					wireColor.y += 1f;
				}
				else {
					//wireColor.add(new Vector3f(0f, 0.5f, 0f));
					wireColor.y += 0.5f;
				}
			}

			GLShapeDrawer.drawOpenGL(this.gl, m, colObj.getCollisionShape(), wireColor, world.getDebugDrawer().getDebugMode());
		}
		
		//---------------------------
		
		Camera.acceptInput((float)delta);
		Camera.apply();
		
		Display.update();
	}
	
	@Override
	public void run() {
		final double TEN_TO_NINTH = Math.pow(10, 9);
		final double ONE_OVER_FPS = 1.0 / 60.0;
		final double ONE_OVER_PHYSICS_FPS = 1.0 / 24.0;
		double accumulator = 0;
		double network_accumulator = 0;
		double lastTime = System.nanoTime() / TEN_TO_NINTH;;
		
		spawnLevel();
		
		//TODO: delete
		if(DEBUG_WORLD) {
			world.setDebugDrawer(new GLDebugDrawer(gl));
			try {
	            Display.setDisplayMode(new DisplayMode(800,600));
	            Display.create();
	            Display.makeCurrent();
	        } catch (LWJGLException e) {
	            e.printStackTrace();
	            System.exit(0);
	        }
			//Mouse.setGrabbed(true);
	        Camera.create();
	        GL11.glMatrixMode(GL11.GL_PROJECTION);
	        GL11.glLoadIdentity();
	        GLU.gluPerspective(45.0f,(float)800/(float)600,0.1f,1000.0f);
	        GL11.glMatrixMode(GL11.GL_MODELVIEW);
	        GL11.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		        
		while (!Thread.interrupted()) {
			
			//TODO: delete
			if(DEBUG_WORLD) 
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
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
			
			//TODO: delete
			if(DEBUG_WORLD)
				render((float)delta);
		}
		
		world.destroy();
		
		//TODO: delete
		if(DEBUG_WORLD)
			Display.destroy();
	}
	
	@SuppressWarnings("unused")
	private RigidBody spawnCube() {
		CollisionShape cubeShape = new BoxShape(new Vector3f(1,1,1));
		Transform cubeTransform = new Transform();
		cubeTransform.setRotation(new Quat4f(0, 0, 0, 1));
		cubeTransform.origin.set(new float[] {0, 20, 0});
		DefaultMotionState cubeMotionState = new DefaultMotionState(cubeTransform);
		
		RigidBodyConstructionInfo cubeRigidBodyCI = new RigidBodyConstructionInfo(10, cubeMotionState, cubeShape, new Vector3f(0, 1, 0));
		RigidBody cubeRigidBody = new RigidBody(cubeRigidBodyCI);
		
		world.addRigidBody(cubeRigidBody);
		
		return cubeRigidBody;
	}
	
	@SuppressWarnings("unused")
	private void spawnFloor() {
		CollisionShape groundShape = new BoxShape(new Vector3f(50, 3, 50));
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
		players.add(new Player(u, world, new Vector3f(0,25,0)));
	}
	
	private RigidBody spawnLevel() {
		try {
			Build builder = new Build();
			new Parse(builder, "C:/Users/lbitt/Documents/Projetos/Programacao/Tag/Client/Assets/Tag/Models/level_test1.obj");
			
			ArrayList<Integer> indices = new ArrayList<Integer>();
			ArrayList<Float> vertices = new ArrayList<Float>();
			
			int i = 0;
			for(Face face : builder.faces) {
				for(FaceVertex fv : face.vertices) {
					indices.add(i++);
					
					vertices.add(fv.v.x);
					vertices.add(fv.v.y);
					vertices.add(fv.v.z);
				}
			}
			
			int numTriangles = indices.size() / 3;
			ByteBuffer triangleIndexBase = ByteBuffer.allocateDirect(numTriangles * 3 * Integer.BYTES).order(ByteOrder.nativeOrder());
			triangleIndexBase.asIntBuffer().put(Util.convertIntegers(indices));
			int triangleIndexStride = 3 * 4;
			int numVertices = vertices.size() / 3;
			ByteBuffer vertexBase = ByteBuffer.allocateDirect(numVertices * 3 * Float.BYTES).order(ByteOrder.nativeOrder());
			vertexBase.asFloatBuffer().put(Util.convertFloats(vertices));
			int vertexStride = 3 * 4;
			
			TriangleIndexVertexArray triangleArray = new TriangleIndexVertexArray(numTriangles, triangleIndexBase, triangleIndexStride, numVertices, vertexBase, vertexStride);
			BvhTriangleMeshShape level = new BvhTriangleMeshShape(triangleArray, false);
			
			//---------------------------//
			
			Transform groundTransform = new Transform();
			groundTransform.setRotation(new Quat4f(0.f, 0.f, 0.f, 1.f));
			groundTransform.origin.set(new float[] { 0, 0, 0 });
			DefaultMotionState groundMotionState = new DefaultMotionState(groundTransform);
			RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, level, new Vector3f(0, 0, 0));
			groundRigidBodyCI.restitution = 0.5f;
			RigidBody groundRigidBody = new RigidBody(groundRigidBodyCI);

			world.addRigidBody(groundRigidBody);
			
			return groundRigidBody;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
