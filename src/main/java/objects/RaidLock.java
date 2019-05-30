package objects;

import java.time.Instant;

import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.VerificationLevel;

public class RaidLock {
	
	VerificationLevel normalVerificationLevel;
	ExplicitContentFilterLevel normalExplicitContentFilterLevel;
	
	long timer;
	long unlockAt;
	
	public RaidLock(long timer, VerificationLevel verificationLevel, ExplicitContentFilterLevel explicitContentFilterLevel) {
		this.timer = timer;
		this.unlockAt = Instant.now().plusMillis(timer).toEpochMilli();
		this.normalVerificationLevel = verificationLevel;
		this.normalExplicitContentFilterLevel = explicitContentFilterLevel;
	}
	
	public VerificationLevel getNormalVerificationLevel() {
		return normalVerificationLevel;
	}
	public void setNormalVerificationLevel(VerificationLevel normalVerificationLevel) {
		this.normalVerificationLevel = normalVerificationLevel;
	}
	public ExplicitContentFilterLevel getNormalExplicitContentFilterLevel() {
		return normalExplicitContentFilterLevel;
	}
	public void setNormalExplicitContentFilterLevel(ExplicitContentFilterLevel normalExplicitContentFilterLevel) {
		this.normalExplicitContentFilterLevel = normalExplicitContentFilterLevel;
	}
	public long getTimer() {
		return timer;
	}
	public void setTimer(long timer) {
		this.timer = timer;
	}
	public long getUnlockAt() {
		return unlockAt;
	}
	public void setUnlockAt(long unlockAt) {
		this.unlockAt = unlockAt;
	}
}
