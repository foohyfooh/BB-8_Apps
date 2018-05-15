package com.foohyfooh.bb8;

import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryStateChangedListener;
import com.orbotix.common.RobotChangedStateListener;

public interface DiscoveryListener extends DiscoveryStateChangedListener, DiscoveryAgentEventListener,
        RobotChangedStateListener {
}
