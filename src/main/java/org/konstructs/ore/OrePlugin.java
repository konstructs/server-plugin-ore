package org.konstructs.ore;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.Props;

import konstructs.plugin.KonstructsActor;
import konstructs.plugin.PluginConstructor;
import konstructs.api.*;
import konstructs.api.messages.*;

public class OrePlugin extends KonstructsActor {
    private final List<OreConfig> configs;
    private final Random random = new Random();

    public OrePlugin(ActorRef universe, List<OreConfig> configs) {
        super(universe);
        this.configs = configs;

        for(OreConfig config: configs) {
            getContext().actorOf(Ore.props(getUniverse(), config));
        }
    }

    @Override
    public void onBlockUpdateEvent(BlockUpdateEvent event) {
        int select = random.nextInt(configs.size());
        int i = 0;

        for(ActorRef child: getContext().getChildren()) {
            if(i == select) {
                child.tell(event, getSender());
            }
            i++;
        }
    }

    @PluginConstructor
    public static Props
        props(
              String pluginName,
              ActorRef universe,
              Config config) {
        Class currentClass = new Object() { }.getClass().getEnclosingClass();


        ArrayList<OreConfig> configs = new ArrayList<>();

        Config oresConfig = config.getConfig("ores");

        for(String key: oresConfig.root().keySet()) {
            configs.add(new OreConfig(key, oresConfig.getConfig(key)));
        }
        return Props.create(currentClass, universe, configs);
    }

}
