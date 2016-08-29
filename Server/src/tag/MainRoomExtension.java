package tag;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

import tag.user.UserJoinRoomHandler;
import tag.user.UserLeaveRoomHandler;

public class MainRoomExtension extends SFSExtension {

	@Override
	public void init() {
		trace("MainRoomExtension: initializing...");
		
		addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomHandler.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveRoomHandler.class);
		
		trace("MainRoomExtension: initialization completed.");
	}
	
	@Override
	public void destroy() {
		trace("MainRoomExtension: destroying...");
		super.destroy();
		trace("MainRoomExtension: destruction completed.");
	}

}
