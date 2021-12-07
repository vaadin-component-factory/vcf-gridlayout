package com.vaadin.componentfactory.gridlayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {

  public MainView() {
    this.setMargin(true);
    
    // Create a 4 by 4 grid layout.
    GridLayout grid = new GridLayout(4, 4);
//    grid.setSpacing(true);
//    grid.setMargin(true);
    
    // NEED TO DEFINE WIDTH & HEIGHT
    grid.setWidth("404px");
    grid.setHeight("348px");   


    // Fill out the first row using the cursor.
    Button brc1 = new Button("R/C 1");
    brc1.setWidth("100%");
    grid.addComponent(brc1);

    for (int i = 0; i < 3; i++) {
        int col = grid.getCursorX() + 1;
        Button b  = new Button("Col " + col);
        b.setWidth("100%");
        grid.addComponent(b);
    }

    // Fill out the first column using coordinates.
    for (int i = 1; i < 4; i++) {
        Button b = new Button("Row " + i);
        b.setHeight("100%");
        grid.addComponent(b, 0, i);
    }

    // // Add some components of various shapes.
    Button b = new Button("3x1 button");
    grid.addComponent(b, 1, 1, 3, 1);
    grid.addComponent(new Label("1x2 cell"), 1, 2, 1, 3);
    Div div = new Div();
    div.setText("A 2x2 div field");
    div.setWidth("252px");
    div.setHeight("248px");
    grid.addComponent(div, 2, 2, 3, 3);
        
    add(grid);
  }
}
