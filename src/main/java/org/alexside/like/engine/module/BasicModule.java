package org.alexside.like.engine.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.alexside.like.engine.config.AppConfig;
import org.alexside.like.engine.db.CqlConnector;
import org.alexside.like.engine.service.LikeService;
import org.alexside.like.engine.service.impl.LikeServiceImpl;

public class BasicModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AppConfig.class)
                .asEagerSingleton();
        bind(CqlConnector.class)
                .in(Scopes.SINGLETON);
        bind(LikeService.class)
                .to(LikeServiceImpl.class)
                .in(Scopes.SINGLETON);
    }
}
