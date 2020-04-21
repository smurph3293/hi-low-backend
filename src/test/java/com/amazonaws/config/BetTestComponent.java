package com.amazonaws.config;

import com.amazonaws.dao.BetDao;
import com.amazonaws.handler.BetHandlerTestBase;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {BetModule.class})
public interface BetTestComponent {
    BetDao provideBetDao();
    void inject(BetHandlerTestBase integrationTest);
}
