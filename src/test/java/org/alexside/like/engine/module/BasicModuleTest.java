package org.alexside.like.engine.module;

import com.google.inject.Scopes;
import org.alexside.like.engine.game.GameGenerator;
import org.alexside.like.engine.service.LikeServiceTest;
import org.alexside.like.engine.service.impl.LikeServiceTestImpl;

public class BasicModuleTest extends BasicModule {
    @Override
    protected void configure() {
        super.configure();
        bind(LikeServiceTest.class)
                .to(LikeServiceTestImpl.class)
                .in(Scopes.SINGLETON);
        bind(GameGenerator.class)
                .in(Scopes.SINGLETON);
    }
}