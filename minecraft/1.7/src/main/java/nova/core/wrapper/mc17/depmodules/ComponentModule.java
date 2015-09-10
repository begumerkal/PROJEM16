package nova.core.wrapper.mc17.depmodules;

import nova.core.entity.component.RigidBody;
import nova.core.wrapper.mc17.wrapper.entity.forward.BWRigidBody;
import se.jbee.inject.bind.BinderModule;

/**
 * @author Calclavia
 */
public class ComponentModule extends BinderModule {

	@Override
	protected void declare() {
		bind(RigidBody.class).to(BWRigidBody.class);
	}
}