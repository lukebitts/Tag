package tag;

import java.util.ArrayList;

import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.CreateRoomSettings.RoomExtensionSettings;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;

public class QueueManager extends Thread {
	
	MainZoneExtension ext;
	
	public static final int USERS_NEEDED_FOR_GAME = 2; 
	
	public QueueManager(MainZoneExtension ext) {
		this.ext = ext;
	}
	
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				//e.printStackTrace();
				//This means the game isn't running anymore
				break;
			}
			
			try {
				ArrayList<User> users = new ArrayList<User>();
				if(this.ext.userQueue.size() >= USERS_NEEDED_FOR_GAME) {
					while(users.size() < USERS_NEEDED_FOR_GAME) {
						users.add(this.ext.getApi().getUserById(this.ext.userQueue.take()));
					}
					
					ext.trace(String.format("Got enough (%s) users for a game!", USERS_NEEDED_FOR_GAME));
					
					try {
						startGame(users);
					}
					catch(SFSCreateRoomException e) {
						for(User u : users) {
							if(u.isConnected())
								this.ext.userQueue.add(u.getId());
						}
						
						e.printStackTrace();
					}
					catch(SFSJoinRoomException e) {
						for(User u : users) {
							if(u.isConnected())
								this.ext.userQueue.add(u.getId());
						}
						
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				//This means the userQueue is not available anymore, so we stop this thread
				break;
			}
		}
	}
	
	void startGame(ArrayList<User> users) throws SFSCreateRoomException, SFSJoinRoomException {
		
		if(users.size() != USERS_NEEDED_FOR_GAME) {
			throw new RuntimeException("Got a list of users with the wrong number of players");
		}
		
		CreateRoomSettings roomSettings = new CreateRoomSettings();
		roomSettings.setName(users.get(0).getName());
		roomSettings.setMaxUsers(USERS_NEEDED_FOR_GAME);
		roomSettings.setMaxVariablesAllowed(100);
		roomSettings.setGame(true);
		roomSettings.setDynamic(true);
		//roomSettings.setRoomSettings(EnumSet.of(SFSRoomSettings.USER_ENTER_EVENT, SFSRoomSettings.USER_EXIT_EVENT));
		
		RoomExtensionSettings extensionSettings = new RoomExtensionSettings("TagExtension", "tag.MainRoomExtension");
		
		roomSettings.setExtension(extensionSettings);
		
		ext.getApi().createRoom(ext.getParentZone(), roomSettings, null, true, null, true, true);

		SFSObject message = new SFSObject();
		message.putUtfString("room_name", roomSettings.getName());
		this.ext.send("game_found", message, users);
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
	}
}
