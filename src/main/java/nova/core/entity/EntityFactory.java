package nova.core.entity;

import nova.core.util.Factory;
import nova.internal.dummy.Wrapper;

import java.util.function.Function;

/**
 * @author Calclavia
 */
public class EntityFactory extends Factory<Entity> {

	//TODO: This is not the optimal way, especially when we have more arguments to pass...
	public EntityFactory(Function<Object[], Entity> constructor) {
		super(constructor);
	}

	public Entity makeEntity(Wrapper wrapper, Object... args) {
		Entity newEntity = constructor.apply(args);
		newEntity.add(wrapper);
		return newEntity;
	}
}
