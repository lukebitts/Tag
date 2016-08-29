package tag;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

import tag.user.LoginEventHandler;
import tag.user.UserDisconnectHandler;

public class MainZoneExtension extends SFSExtension {

	@Override
	public void init() {
		trace("MainZoneExtension: initializing...");
		
		addEventHandler(SFSEventType.USER_LOGIN, LoginEventHandler.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, UserDisconnectHandler.class);
		
		trace("MainZoneExtension: initialization completed.");
	}
	
	@Override
	public void destroy() {
		trace("MainZoneExtension: destroying...");
		super.destroy();
		trace("MainZoneExtension: destruction completed.");
	}

}
