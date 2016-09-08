package tag;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

import tag.user.UserDisconnectHandler;
import tag.user.UserJoinZoneHandler;

import java.util.concurrent.LinkedBlockingQueue;

public class MainZoneExtension extends SFSExtension {

	QueueManager queueManager;
	
	//public ConcurrentLinkedQueue<Integer> userQueue;
	public LinkedBlockingQueue<Integer> userQueue;
	
	@Override
	public void init() {
		trace("MainZoneExtension: initializing...");
		
		addEventHandler(SFSEventType.USER_JOIN_ZONE, UserJoinZoneHandler.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, UserDisconnectHandler.class);
		
		userQueue = new LinkedBlockingQueue<Integer>();
		
		queueManager = new QueueManager(this);
		queueManager.start();
		
		trace("MainZoneExtension: initialization completed.");
	}
	
	@Override
	public void destroy() {
		trace("MainZoneExtension: destroying...");
		super.destroy();
		
		queueManager.interrupt();
		try {
			queueManager.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		trace("MainZoneExtension: destruction completed.");
	}

}
