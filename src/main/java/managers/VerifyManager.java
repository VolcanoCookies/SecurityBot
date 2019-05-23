package managers;

import java.util.HashMap;
import java.util.Map;

import objects.VerifyRequest;

public class VerifyManager {
	Map<Long, VerifyRequest> verifyRequests = new HashMap<>();
	
	private void newVerifyRequest(VerifyRequest verifyRequest) {
		
		verifyRequests.put(verifyRequest.serverID, verifyRequest);
		
	}
}
