package nova.core.gui;

import nova.core.gui.ComponentEvent.ActionEvent;
import nova.core.gui.elements.Button;
import nova.core.gui.layout.BorderLayout;
import nova.core.gui.layout.BorderLayout.EnumBorderRegion;
import nova.core.gui.layout.LayoutConstraints.BorderLayoutConstraints;
import nova.core.network.NetworkTarget.Side;

public class GuiTest {

	// TODO Make this a test case, for now it's just to test generics and stuffs
	// until the core is functional.
	public void testGui() {

		//Safe way of getting the fitting constraints for the layout, could also create the object yourself and spare another line.
		BorderLayout layout = new BorderLayout();
		BorderLayoutConstraints constraints = layout.constraints();
		GuiContainer container = new GuiContainer("test").setLayout(layout)
			.addElement(new Button("testButton1")
				.registerEventListener(this::onButton1Pressed, ActionEvent.class, Side.SERVER), 
				constraints.of(e -> e.region = EnumBorderRegion.WEST))
			.addElement(new Button("testButton2"), constraints.of(e -> e.region = EnumBorderRegion.CENTER))
			.addElement(new Button("testButton3"), constraints.of(e -> e.region = EnumBorderRegion.EAST));
	}
	
	private void onButton1Pressed(ActionEvent event) {
		
	}
}