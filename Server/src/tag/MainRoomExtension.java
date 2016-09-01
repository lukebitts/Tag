package tag;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

import tag.game.Game;
import tag.user.UserJoinRoomHandler;
import tag.user.UserLeaveRoomHandler;

public class MainRoomExtension extends SFSExtension {

	public Game game;
	
	@Override
	public void init() {
		trace("MainRoomExtension: initializing...");
		
		addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomHandler.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveRoomHandler.class);
		
		game = new Game(this.getParentRoom());
		
		trace("MainRoomExtension: initialization completed.");
	}
	
	@Override
	public void destroy() {
		trace("MainRoomExtension: destroying...");
		super.destroy();
		
		game.interrupt();
		try {
			game.join();
		} catch (InterruptedException e) {
			trace(e);
		}
		
		trace("MainRoomExtension: destruction completed.");
	}

}
