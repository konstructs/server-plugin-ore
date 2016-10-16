# server-plugin-ore
Generates ore veins

## Add your own ore
Other plugins can register ore blocks with this plugin by extending its configuration:
```
konstructs {
  ...
  org/konstructs/ore.ores {
    // The config key is the block type of the ore, i.e.
    // the block type of the ore is here my/domain/my-ore
    my/domain/my-ore {
      // This is the block type in which the ore can spawn
      spawns-in = org/konstructs/stone

      // This is the radius of the cube that needs to be of
      // the spawns-in type for the ore to spawn. A radius of 3 is equal to a
      // cube of 7x7x7 = 343 blocks (a block in the middle and 3 blocks in all directions)
      radius = 3

      // This is the number of generations the L-system will grow the vein
      // Simplified, the number of blocks in the vein is 2^generations, so
      // 5 generations is roughly equal to a vein of 32 blocks (can be both
      // smaller and larger).
      generations = 5

      // This is the probability that the ore will appear (the plugin tries to spawn a vein).
      // The radius is applied after this, so it is not certain that the ore will be able to
      // spawn.
      // The value is divided with 10000, so here there is a 1% probability of spawning this
      // ore.
      probability = 100

      // This is the maximum distance from the triggering event (removal of a block of type
      // spawns-in) that the vein will spawn. The minimum distance is always the radius + 3.
      max-distance = 20
    }
  }
  ...
}
```
## Design notes

The L-System can outgrow the radius. A worst case scenario for vein growth is a straight line.
On the other hand,  statistically the vein is much shorter than the maximum number of blocks. If you
want to be 100% certain that an ore block will never appear in a visible stone block, then you must use
a small number of generations and a large enough value for radius. E.g. a value of 2 generations, might
generate a 2^2 = 4 blocks long vein and therefore the radius must be at least 5 to contain it fully.
It is important to notice that a radius of 5 will already generate a block query for 11x11x11 = 1331 block,
while this is fully feasible, a larger radius like 20 (41x41x41=68921) will already start consuming noticeable
server performance. Therefore, since the vein will only replace blocks of the type spawns-in, usually
it is preferable to allow the vein to appear in visible blocks, since the probability of the straight line
growth is very low.
