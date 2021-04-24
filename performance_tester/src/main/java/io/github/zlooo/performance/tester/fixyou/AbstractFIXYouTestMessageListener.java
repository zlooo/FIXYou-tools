package io.github.zlooo.performance.tester.fixyou;

import io.github.zlooo.fixyou.commons.pool.ObjectPool;
import io.github.zlooo.fixyou.netty.AbstractNettyAwareFixMessageListener;
import io.github.zlooo.fixyou.parser.model.FixMessage;
import lombok.Setter;

public abstract class AbstractFIXYouTestMessageListener extends AbstractNettyAwareFixMessageListener {

    @Setter
    protected ObjectPool<FixMessage> fixMessageObjectPool;

    protected FixMessage getFixMessageFromPool() {
        FixMessage fixMessage;
        while ((fixMessage = fixMessageObjectPool.tryGetAndRetain()) == null) {
            Thread.yield();
        }
        return fixMessage;
    }
}
