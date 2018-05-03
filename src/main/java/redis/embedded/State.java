package redis.embedded;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Allows atomic access to {@link RedisState} state object
 */
public class State {

    private static final Set<RedisState> ELIGIBLE_FOR_STOP = EnumSet.of(RedisState.activating, RedisState.active);
    private static final Set<RedisState> ELIGIBLE_FOR_START = EnumSet.of(RedisState.inactive, RedisState.failed);
    private static final Set<RedisState> ELIGIBLE_FOR_FAIL = EnumSet.of(RedisState.activating, RedisState.deactivating);

    private AtomicReference<RedisState> state = new AtomicReference<>(RedisState.inactive);

    public RedisState getState() {
        return state.get();
    }

    public boolean isActive() {
        return getState() == RedisState.active;
    }

    public boolean isActivating() {
        return getState() == RedisState.activating;
    }

    public boolean isDeactivating() {
        return getState() == RedisState.deactivating;
    }

    public boolean setActivating() {
        return checkAndSetState(ELIGIBLE_FOR_START, RedisState.activating);
    }

    public boolean setActive() {
        return checkAndSetState(RedisState.activating, RedisState.active);
    }

    public boolean setFailed() {
        return checkAndSetState(ELIGIBLE_FOR_FAIL, RedisState.failed);
    }

    public boolean setDeactivating() {
        return checkAndSetState(ELIGIBLE_FOR_STOP, RedisState.deactivating);
    }

    public boolean setDeactivatingByTimeout() {
        return checkAndSetState(RedisState.activating, RedisState.deactivating);
    }

    public boolean setInactive() {
        return checkAndSetState(RedisState.deactivating, RedisState.inactive);
    }

    boolean checkAndSetState(RedisState expect, RedisState update) {
        return state.compareAndSet(expect, update);
    }

    public boolean checkAndSetState(Collection<RedisState> expectStates, RedisState update) {
        for (RedisState expect : expectStates) {
            if (state.compareAndSet(expect, update)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return state.get().name();
    }
}
