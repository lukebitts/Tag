package tag.game;

import java.util.Arrays;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
//import com.bulletphysics.dynamics.character.KinematicCharacterController;
import tag.util.KinematicCharacterController;
import com.bulletphysics.linearmath.Transform;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSRuntimeException;
import tag.user.InputRequestHandler;
import tag.util.Util;

public class Player {

	private User sfsUser;
	private KinematicCharacterController controller;
	private PairCachingGhostObject ghostObject;
	private Quat4f orientation = new Quat4f(0, 0, 0, 1);
	private float turningTime = 0f;
	private boolean wasBackwards = false;
	private boolean isBeingChased = false; 
	
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
	
	private boolean setPosition(Vector3f position, boolean force) {
		if(!force && this.getPosition().equals(position)) return false;
		
		controller.warp(position);
		updatePositionVariables();
		
		assert(getPosition().equals(position));
		
		return true;
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
	
	public void updateInput(DiscreteDynamicsWorld world, float delta) {
		Boolean[] input = getInput();
		
		turningTime -= delta;
		
		if(turningTime > 0) {
			return;
		}
		
		int z = input[InputRequestHandler.INPUT_TYPE.FORWARD] ? 1 : 0;
		
		if (input[InputRequestHandler.INPUT_TYPE.BACKWARD]) {
			if(!wasBackwards) {
				wasBackwards = true;
				orientation.mul(new Quat4f(0f, 1.0f, 0f, 0f));
				
				if(z == 1) {
					turningTime = 0.85f;
				}
				else {
					turningTime = 0.2f;
				}
			}
		}
		else {	
			wasBackwards = false;
			Quat4f extraOrientationLeft = new Quat4f(0f, -1.8f * delta, 0f, 1.0f);
			Quat4f extraOrientationRight = new Quat4f(0f, 1.8f * delta, 0f, 1.0f);
			
			if(input[InputRequestHandler.INPUT_TYPE.LEFT]) {
				orientation.mul(extraOrientationLeft);
			}
			if(input[InputRequestHandler.INPUT_TYPE.RIGHT]) {
				orientation.mul(extraOrientationRight);
			}
		}
		
		Vector3f direction = Util.mulQuatVec(orientation, new Vector3f(0, 0, z * 8.f * delta));
		
		controller.setWalkDirection(direction);
		controller.updateAction(world, delta);
	}
	
	public void updatePositionVariables() {
		Vector3f position = getPosition();
		
		try {
			SFSArray posData = new SFSArray();
			posData.addFloat(position.x);
			posData.addFloat(position.y);
			posData.addFloat(position.z);
			
			SFSArray oriData = new SFSArray();
			oriData.addFloat(orientation.x);
			oriData.addFloat(orientation.y);
			oriData.addFloat(orientation.z);
			oriData.addFloat(orientation.w);
			
			UserVariable pos = new SFSUserVariable("pos", posData);
			UserVariable ori = new SFSUserVariable("ori", oriData);
			UserVariable turning = new SFSUserVariable("turning", turningTime > 0); 
			
			SmartFoxServer.getInstance().getAPIManager().getSFSApi().setUserVariables(sfsUser, Arrays.asList(pos, ori, turning));
		}
		catch (SFSRuntimeException e) {
			e.printStackTrace();
		}
	}

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
	    float characterHeight = 1.50f * 1;
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

	public boolean isBeingChased() {
		return isBeingChased;
	}

	public void setBeingChased(boolean isBeingChased) {
		this.isBeingChased = isBeingChased;
	}
	
}
