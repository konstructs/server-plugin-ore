package org.konstructs.ore;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.Props;

import konstructs.plugin.KonstructsActor;
import konstructs.plugin.PluginConstructor;
import konstructs.api.*;
import konstructs.api.messages.*;

public class OrePlugin extends KonstructsActor {

    private static class OreEntry {
        private final OreConfig config;
        private final ActorRef actor;

        public OreEntry(OreConfig config, ActorRef actor) {
            this.config = config;
            this.actor = actor;
        }

        public OreConfig getConfig() {
            return config;
        }

        public ActorRef getActor() {
            return actor;
        }

    }

    private final Map<BlockTypeId, List<OreEntry>> ores = new HashMap<>();
    private final Random random = new Random();

    public OrePlugin(ActorRef universe, List<OreConfig> configs) {
        super(universe);

        for(OreConfig config: configs) {
            ActorRef actor = getContext().actorOf(Ore.props(getUniverse(), config));
            if(ores.containsKey(config.getSpawnsIn())) {
                ores.get(config.getSpawnsIn()).add(new OreEntry(config, actor));
            } else {
                ArrayList entries = new ArrayList();
                entries.add(new OreEntry(config, actor));
                ores.put(config.getSpawnsIn(), entries);
            }
        }
    }

    @Override
    public void onBlockUpdateEvent(BlockUpdateEvent event) {
        for(Map.Entry<Position, BlockUpdate> p: event.getUpdatedBlocks().entrySet()) {
            List<OreEntry> entries = ores.get(p.getValue().getBefore());
            if(entries != null) {
                int total = 0;
                for(OreEntry entry: entries) {
                    total += entry.getConfig().getProbability();
                }
                int selected = random.nextInt(total);
                int sum = 0;
                for(OreEntry entry: entries) {
                    OreConfig config = entry.getConfig();
                    sum += config.getProbability();
                    if(sum < selected) {
                        if(random.nextInt(10000) <= config.getProbability())
                            entry.getActor().tell(new SpawnVein(p.getKey()), getSelf());
                        return;
                    }
                }
            }
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
