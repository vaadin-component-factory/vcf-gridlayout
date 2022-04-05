package com.vaadin.componentfactory.gridlayout;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route
public class MainView extends DemoView {

  @Override
  protected void initView() {
    createBasicGridLayoutExample();
    createBasicGridLayoutWidthExample();
    createBasicGridLayoutWidth2Example();
    createBasicGridLayoutWidth3Example();
  }
  
  public void createBasicGridLayoutExample() {
    // begin-source-example
    // source-example-heading: Simple grid layout example
    // Create a 4 by 4 grid layout.
    GridLayout grid = new GridLayout(4, 4);
    grid.setSpacing(true);
    grid.setMargin(true);
    
    // Need to define width & height for the layout.
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

    // Add some components of various shapes.
    Button b = new Button("3x1 button");
    b.setWidth("100%");
    grid.addComponent(b, 1, 1, 3, 1);
    grid.addComponent(new Label("1x2 cell"), 1, 2, 1, 3);
    Div div = new Div();
    div.setText("A 2x2 div field");
    div.setWidth("252px");
    div.setHeight("248px");
    grid.addComponent(div, 2, 2, 3, 3);
            
    // end-source-example
    grid.setId("simple-grid-layout-example");
    addCard("Simple grid layout example", grid);
  } 

  public void createBasicGridLayoutWidthExample() {
    // begin-source-example
    // source-example-heading: Simple grid layout example
    // Create a 4 by 4 grid layout.
    GridLayout grid = new GridLayout(4, 4);
    grid.setSpacing(false);
    grid.setMargin(false);
    
    // Need to define width & height for the layout.
    grid.setWidthFull();
    grid.setHeight("30px");   

    Button b1  = new Button("10%");
    b1.setWidth("100%");
    grid.addComponent(b1);
    
    Button b2  = new Button("Auto");
    b2.setWidth("100%");
    b2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    grid.addComponent(b2);

    Button b3  = new Button("30%");
    b3.setWidth("100%");
    grid.addComponent(b3);

    Button b4  = new Button("40%");
    b4.setWidth("100%");
    b4.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    grid.addComponent(b4);
    
    
    grid.setColumnWidth(1, "10%");
    //grid.setColumnWidth(2, "20%");
    grid.setColumnWidth(3, "30%");
    grid.setColumnWidth(4, "40%");
            
    // end-source-example
    grid.setId("simple-grid-layout-width-example");

    addCard("Grid layout width %", grid);
  } 

  public void createBasicGridLayoutWidth2Example() {
    // begin-source-example
    // source-example-heading: Simple grid layout example
    // Create a 4 by 4 grid layout.
    GridLayout grid = new GridLayout(4, 4);
    grid.setSpacing(false);
    grid.setMargin(false);
    
    // Need to define width & height for the layout.
    grid.setWidthFull();
    grid.setHeight("30px");   

    Button b1  = new Button("10em");
    b1.setWidth("100%");
    grid.addComponent(b1);
    
    Button b2  = new Button("Auto");
    b2.setWidth("100%");
    b2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    grid.addComponent(b2);

    Button b3  = new Button("10em");
    b3.setWidth("100%");
    grid.addComponent(b3);

    Button b4  = new Button("20em");
    b4.setWidth("100%");
    b4.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    grid.addComponent(b4);
    
    
    grid.setColumnWidth(1, 10, Unit.EM);
    //grid.setColumnWidth(2, "20%");
    grid.setColumnWidth(3, 10, Unit.EM);
    grid.setColumnWidth(4, 20, Unit.EM);
            
    // end-source-example
    grid.setId("simple-grid-layout-width2-example");

    addCard("Grid layout width in em", grid);
  } 

  public void createBasicGridLayoutWidth3Example() {
    // begin-source-example
    // source-example-heading: Simple grid layout example
    // Create a 4 by 4 grid layout.
    GridLayout grid = new GridLayout(4, 4);
    grid.setSpacing(false);
    grid.setMargin(false);
    
    // Need to define width & height for the layout.
    grid.setWidthFull();
    grid.setHeight("30px");   

    Button b1  = new Button("1");
    b1.setWidth("100%");
    grid.addComponent(b1);
    
    Button b2  = new Button("1");
    b2.setWidth("100%");
    b2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    grid.addComponent(b2);

    Button b3  = new Button("2");
    b3.setWidth("100%");
    grid.addComponent(b3);

    Button b4  = new Button("4");
    b4.setWidth("100%");
    b4.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    grid.addComponent(b4);
    
    
    grid.setColumnExpandRatio(1, 1);
    grid.setColumnExpandRatio(2, 1);
    grid.setColumnExpandRatio(3, 2);
    grid.setColumnExpandRatio(4, 4);
            
    // end-source-example
    grid.setId("simple-grid-layout-expand-ratio-example");

    addCard("Grid layout expand ratio", grid);
  } 
}
