package redis.embedded;

import java.lang.annotation.*;

/**
 * A simple annotation which let us have a quick view of what the test configuration is.
 * This annotation do _absolutely nothing_
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.SOURCE)
@interface Configuration {
    int master() default 0;

    int slave() default 0;

    int sentinel() default 0;
}
