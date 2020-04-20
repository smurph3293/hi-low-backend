package com.amazonaws.config;

import com.amazonaws.handler.CreateBetHandler;
import com.amazonaws.handler.DeleteBetHandler;
import com.amazonaws.handler.GetBetHandler;
import com.amazonaws.handler.GetBetsHandler;
import com.amazonaws.handler.UpdateBetHandler;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {OrderModule.class})
public interface OrderComponent {

    void inject(CreateBetHandler requestHandler);

    void inject(DeleteBetHandler requestHandler);

    void inject(GetBetHandler requestHandler);

    void inject(GetBetsHandler requestHandler);

    void inject(UpdateBetHandler requestHandler);
}
