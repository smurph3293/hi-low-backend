package com.amazonaws.config;

import com.amazonaws.handler.CreateBetHandler;
import com.amazonaws.handler.DeleteBetHandler;
import com.amazonaws.handler.GetBetHandler;
import com.amazonaws.handler.UpdateBetHandler;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {BetModule.class})
public interface BetComponent {

    void inject(CreateBetHandler requestHandler);

    void inject(DeleteBetHandler requestHandler);

    void inject(GetBetHandler requestHandler);

    void inject(UpdateBetHandler requestHandler);
}
