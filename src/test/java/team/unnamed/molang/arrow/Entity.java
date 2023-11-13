package team.unnamed.molang.arrow;

class Entity {
    World world;
    int location;
    String name;

    Entity(World world, int location, String name) {
        this.world = world;
        this.location = location;
        this.name = name;

        world.entities[location] = this;
    }

    @Override
    public String toString() {
        return name;
    }
}
