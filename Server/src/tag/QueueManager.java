package tag;

import java.util.EnumSet;

import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.CreateRoomSettings.RoomExtensionSettings;
import com.smartfoxserver.v2.entities.SFSRoomSettings;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;

public class QueueManager extends Thread {
	
	MainZoneExtension ext;
	
	public QueueManager(MainZoneExtension ext) {
		this.ext = ext;
	}
	
	@Override
	public void run() {
		while(!Thread.interrupted()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				
			}
			
			//if(this.ext.userQueue.size() >= 1) {
			try {
				Integer userId = this.ext.userQueue.take();
				
				if(userId != null) {
					User user1 = this.ext.getApi().getUserById(userId);
					
					//User user2 = this.ext.getApi().getUserById(this.ext.userQueue.remove());
					
					ext.trace(String.format("Got enough users for a game! (%s)", user1.getName()));
					
					try {
						startGame(user1, null);
					}
					catch(SFSCreateRoomException e) {
						this.ext.userQueue.add(user1.getId());
						//this.ext.userQueue.add(user2.getId());
						
						ext.trace(e);
					}
					catch(SFSJoinRoomException e) {
						this.ext.userQueue.add(user1.getId());
						
						ext.trace(e);
					}
				}
			} catch (InterruptedException e) {
				ext.trace(e);
			}
		}
	}
	
	void startGame(User user1, User user2) throws SFSCreateRoomException, SFSJoinRoomException {
		
		CreateRoomSettings roomSettings = new CreateRoomSettings();
		roomSettings.setName(user1.getName());
		roomSettings.setMaxUsers(2);
		roomSettings.setMaxVariablesAllowed(0);
		roomSettings.setGame(true);
		roomSettings.setDynamic(true);
		roomSettings.setRoomSettings(EnumSet.of(SFSRoomSettings.USER_ENTER_EVENT, SFSRoomSettings.USER_EXIT_EVENT));
		
		RoomExtensionSettings extensionSettings = new RoomExtensionSettings("TagExtension", "tag.MainRoomExtension");
		
		roomSettings.setExtension(extensionSettings);
		
		ext.getApi().createRoom(ext.getParentZone(), roomSettings, user1, true, null, true, true);
		//room.getUserManager().addUser(user1);
		//room.addUser(user1);
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
	}
}
