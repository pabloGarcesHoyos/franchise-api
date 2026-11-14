package co.com.accenture.blocking;

import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;

public class MongoDriverBlockHoundIntegration implements BlockHoundIntegration {

    @Override
    public void applyTo(BlockHound.Builder builder) {
        builder.allowBlockingCallsInside("com.mongodb.internal.session.ServerSessionPool",
                        "createNewServerSessionIdentifier")
                .allowBlockingCallsInside("com.mongodb.internal.Locks", "checkedWithLock");
    }
}
