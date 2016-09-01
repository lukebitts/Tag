package tag.game;

import java.util.Arrays;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.linearmath.Transform;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.ISFSMMOApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSRuntimeException;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.mmo.Vec3D;

import tag.user.InputRequestHandler;

public class Player {

	private User sfsUser;
	private KinematicCharacterController controller;
	private PairCachingGhostObject ghostObject;
	private Integer physicsCount = 0;
	
	private Boolean[] input = new Boolean[]{false, false, false, false, false};
	
	public Player(User sfsUser, DiscreteDynamicsWorld world, Vector3f position) {
		this.sfsUser = sfsUser;
		
		System.out.println(String.format("Player::Player() pos:[%s, %s, %s]", position.x, position.y, position.z));
		
		createController(world, position);
		
		setPosition(position, true);
		
		this.sfsUser.setProperty("player", this);
	}
	
	public Vector3f getPosition() {
		Transform pos = new Transform();
		ghostObject.getWorldTransform(pos);
		
		return new Vector3f(pos.origin.x, pos.origin.y, pos.origin.z);
	}
	
	public boolean setPosition(Vector3f position) {
		return setPosition(position, false);
	}
	
	public void setInput(Boolean[] input) {
		synchronized (this.input) {
			this.input = input.clone();
		}
	}
	
	public Boolean[] getInput() {
		synchronized (this.input) {
			return input.clone();
		}
	}
	
	private boolean setPosition(Vector3f position, boolean force) {
		if(!force && this.getPosition().equals(position)) return false;
		
		controller.warp(position);
		updatePositionVariables();
		
		assert(getPosition().equals(position));
		
		return true;
	}
	
	public void updateInput(DiscreteDynamicsWorld world, float delta) {
		Boolean[] input = getInput();
		
		int x = input[InputRequestHandler.INPUT_TYPE.LEFT] ? -1 : input[InputRequestHandler.INPUT_TYPE.RIGHT] ? 1 : 0;
		int z = input[InputRequestHandler.INPUT_TYPE.FORWARD] ? 1 : input[InputRequestHandler.INPUT_TYPE.BACKWARD] ? -1 : 0;
		
		float speed = 8.f * delta;
		float div = 1.f;
		if(x != 0 && z != 0)
			div = 1.41f;
		
		controller.setWalkDirection(new Vector3f((float)x/div * speed, 0, (float)z/div * speed));
		
		if(input[InputRequestHandler.INPUT_TYPE.JUMP])
			controller.jump();
		
		controller.updateAction(world, delta);
	}
	
	public void updatePositionVariables() {
		Vector3f position = getPosition();
		
		try {
			SFSArray posData = new SFSArray();
			posData.addFloat(position.x);
			posData.addFloat(position.y);
			posData.addFloat(position.z);
			
			UserVariable pos = new SFSUserVariable("pos", posData);
			SmartFoxServer.getInstance().getAPIManager().getSFSApi().setUserVariables(sfsUser, Arrays.asList(pos));
		}
		catch (SFSRuntimeException e) {
			System.out.println(e);
		}
	}
	
	/*public void updateMmoPosition() {
		Room room = sfsUser.getLastJoinedRoom();
		
		try {
			ISFSMMOApi mmoApi = SmartFoxServer.getInstance().getAPIManager().getMMOApi();
			
			Vector3f position = getPosition();
			
			mmoApi.setUserPosition(sfsUser, new Vec3D(position.x, position.y, position.z), room);
			
			SFSArray posData = new SFSArray();
			posData.addFloat(position.x);
			posData.addFloat(position.y);
			posData.addFloat(position.z);
			
			UserVariable pos = new SFSUserVariable("pos", posData);
			UserVariable physicsCount = new SFSUserVariable("physicsCount", this.physicsCount++);
			
			SmartFoxServer.getInstance().getAPIManager().getSFSApi().setUserVariables(sfsUser, Arrays.asList(pos, physicsCount));
		}
		catch(SFSRuntimeException ex) {
			System.out.println(ex.toString());
			System.out.println("Room: "+room);
		}
	}*/
	
	public User getSfsUser() {
		return sfsUser;
	}
	
	public KinematicCharacterController getController() {
		return controller;
	}
	
	public void destroy(DiscreteDynamicsWorld world) {
		world.removeCollisionObject(ghostObject);
		world.removeAction(controller);
		
		sfsUser.removeProperty("player");
	}
	
	private void createController(DiscreteDynamicsWorld world, Vector3f startVector) {		
		Transform startTransform = new Transform();
	    startTransform.setIdentity();
	    startTransform.origin.set(startVector.x, startVector.y, startVector.z);

	    ghostObject = new PairCachingGhostObject();
	    ghostObject.setWorldTransform(startTransform);
	    float characterHeight = 1.75f * 1;
	    float characterWidth = 0.3f * 1;
	    ConvexShape capsule = new CapsuleShape(characterWidth, characterHeight);
	    ghostObject.setCollisionShape(capsule);
	    ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);
	    ghostObject.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

	    float stepHeight = 0.35f * 1;
	    controller = new KinematicCharacterController(ghostObject, capsule, stepHeight);

	    world.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));
	    world.addAction(controller);
	}
	
}
