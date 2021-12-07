/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.componentfactory.gridlayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CssImport("./styles/grid-layout.css")
public class GridLayout extends Composite<Div> implements HasSize {

  private static final AlignmentInfo ALIGNMENT_DEFAULT = AlignmentInfo.TOP_LEFT;
  
  private static final String SLOT_CLASS_NAME = "v-slot";
    
  private Div div = new Div();

  /**
   * Number of columns in the GridLayout.
   */
  private int columns;

  /**
   * Number of rows in the GridLayout.
   */
  private int rows;

  /**
   * List of components added to the GridLayout.
   */
  private final LinkedList<Component> components = new LinkedList<>();

  private final Map<Component, ChildComponentData> componentsDataMap = new HashMap<>();
  
  /**
   * Default component alignment.
   */
  private Alignment defaultComponentAlignment = Alignment.TOP_LEFT;

  /**
   * Cursor X position: this is where the next component with unspecified x,y is inserted
   */
  private int cursorX = 0;

  /**
   * Cursor Y position: this is where the next component with unspecified x,y is inserted
   */
  private int cursorY = 0;

  private MarginInfo marginInfo;

  @Override
  protected Div initContent() {
    div.getElement().getStyle().set("position", "relative");
    div.setSizeFull();
    return div;
  }

  /**
   * Constructor for a grid of given size (number of columns and rows).
   *
   * The grid may grow or shrink later. Grid grows automatically if you add components outside its
   * area.
   *
   * @param columns Number of columns in the grid.
   * @param rows Number of rows in the grid.
   */
  public GridLayout(int columns, int rows) {
    getElement().getClassList().add("v-gridlayout");
    getElement().getStyle().set("display", "grid");
    setColumns(columns);
    setRows(rows);
  }

  public void setColumns(int columns) {
    // The the param
    if (columns < 1) {
        throw new IllegalArgumentException(
                "The number of columns and rows in the grid must be at least 1");
    }

    // In case of no change
    if (getColumns() == columns) {
        return;
    }
    
    List<String> list = new ArrayList<>();
    list.addAll(Collections.nCopies(columns, "auto"));
    String gridColumns = list.stream().collect(Collectors.joining(" "));
    getElement().getStyle().set("grid-template-columns", gridColumns);
    this.columns = columns;
  }

  /**
   * Get the number of columns in the grid.
   *
   * @return the number of columns in the grid.
   */
  public int getColumns() {
    return this.columns;
  }

  public void setRows(int rows) {
    // The the param
    if (rows < 1) {
        throw new IllegalArgumentException(
                "The number of columns and rows in the grid must be at least 1");
    }

    // In case of no change
    if (getRows() == rows) {
        return;
    }
      
    List<String> list = new ArrayList<>();
    list.addAll(Collections.nCopies(rows, "auto"));
    String gridRows = list.stream().collect(Collectors.joining(" "));
    getElement().getStyle().set("grid-template-rows", gridRows);
    this.rows = rows;
  }

  /**
   * Get the number of rows in the grid.
   *
   * @return the number of rows in the grid.
   */
  public int getRows() {
    return this.rows;
  }

  public void addComponent(Component component) {
    if (component == null) {
      throw new IllegalArgumentException("Component must not be null");
    }

    if (cursorX >= columns) {
      setColumns(cursorX + 1);
    }
    if (cursorY >= rows) {
      setRows(cursorY + 1);
    }

    addComponent(component, cursorX, cursorY);
  }

  public int getCursorX() {
    return cursorX;
  }

  public int getCursorY() {
    return cursorY;
  }

  public void addComponent(Component component, int column, int row) {
    this.addComponent(component, column, row, column, row);
  }

  /**
   * <p>
   * Adds a component to the grid in the specified area. The area is defined by specifying the upper
   * left corner (column1, row1) and the lower right corner (column2, row2) of the area. The
   * coordinates are zero-based.
   * </p>
   *
   * @param component the component to be added, not <code>null</code>.
   * @param column1 the column of the upper left corner of the area the component is supposed to
   *        occupy. The leftmost column has index 0.
   * @param row1 the row of the upper left corner of the area the component is supposed to occupy.
   *        The topmost row has index 0.
   * @param column2 the column of the lower right corner of the area the component is supposed to
   *        occupy.
   * @param row2 the row of the lower right corner of the area the component is supposed to occupy.
   */
  public void addComponent(Component component, int column1, int row1, int column2, int row2) {

    if (component == null) {
      throw new NullPointerException("Component must not be null");
    }

    // Checks that the component does not already exist in the container
    if (components.contains(component)) {
      throw new IllegalArgumentException("Component is already in the container");
    }

    // Checks the validity of the coordinates
    if (column2 < column1 || row2 < row1) {
      throw new IllegalArgumentException("Illegal coordinates for the component");
    }

    // Inserts the component to right place at the list
    // Respect top-down, left-right ordering
    final Iterator<Component> i = components.iterator();
    // final Map<Connector, ChildComponentData> childDataMap = getState().childData;
    int index = 0;
    boolean done = false;
    while (!done && i.hasNext()) {
      components.add(index, component);
      setComponentArea(component, column1, row1, column2, row2);
      done = true;
      index++;
    }
    if (!done) {
      components.addLast(component);
      setComponentArea(component, column1, row1, column2, row2);
    }

    componentsDataMap.put(component, new ChildComponentData(column1, row1, column2, row2));
    
    // Attempt to add to div
    try {
      div.add(component);
//      div.add(wrapComponent(component));
    } catch (IllegalArgumentException e) {
      components.remove(component);
      componentsDataMap.remove(component);
      throw e;
    }

    // update cursor position, if it's within this area; use first position
    // outside this area, even if it's occupied
    if (cursorX >= column1 && cursorX <= column2 && cursorY >= row1 && cursorY <= row2) {
      // cursor within area
      cursorX = column2 + 1; // one right of area
      if (cursorX >= columns) {
        // overflowed columns
        cursorX = 0; // first col
        // move one row down, or one row under the area
        cursorY = (column1 == 0 ? row2 : row1) + 1;
      } else {
        cursorY = row1;
      }
    }
  }

  /**
   * Sets the area where the component is going to be added within the grid layout.
   * 
   * @param component component to be added to the layout
   * @param column1 the column of the upper left corner of the area the component is supposed to
   *        occupy
   * @param row1 the row of the upper left corner of the area the component is supposed to occupy.
   * @param column2 the column of the lower right corner of the area the component is supposed to
   *        occupy.
   * @param row2 the row of the lower right corner of the area the component is supposed to occupy.
   */
  private void setComponentArea(Component component, int column1, int row1, int column2, int row2) {
    component.getElement().getStyle().set("grid-row-start", String.valueOf(row1 + 1));
    component.getElement().getStyle().set("grid-row-end", String.valueOf(row2 + 2));
    component.getElement().getStyle().set("grid-column-start", String.valueOf(column1 + 1));
    component.getElement().getStyle().set("grid-column-end", String.valueOf(column2 + 2));
  }

  private Div wrapComponent(Component component) {
    Div div = new Div();
    div.getElement().setAttribute("class", SLOT_CLASS_NAME);
    div.getElement().appendChild(component.getElement());
    return div;
}
  
  /**
   * Removes all components from this layout.
   */
  public void removeAllComponents() {
    div.removeAll();
    components.clear();
    componentsDataMap.clear();
    cursorX = 0;
    cursorY = 0;
  }

  /**
   * Removes the specified component from the layout.
   *
   * @param component the component to be removed.
   */
  public void removeComponent(Component component) {
    // Check that the component is contained in the container
    if (component == null || !components.contains(component)) {
        return;
    }
    
    components.remove(component);
    div.remove(component);
    componentsDataMap.remove(component);
  }

  /**
   * Gets the number of components contained in the layout.
   *
   * @return the number of contained components
   */
  public int getComponentCount() {
    return components.size();
  }

  /**
   * Replaces the component in the container with another one without changing
   * position.
   *
   * <p>
   * This method replaces component with another one is such way that the new
   * component overtakes the position of the old component. If the old
   * component is not in the container, the new component is added to the
   * container. If the both component are already in the container, their
   * positions are swapped. Component attach and detach events should be taken
   * care as with add and remove.
   * </p>
   *
   * @param oldComponent
   *            the old component that will be replaced.
   * @param newComponent
   *            the new component to be replaced.
   */
  public void replaceComponent(Component oldComponent, Component newComponent) {
    // Gets the locations
    ChildComponentData oldLocation = componentsDataMap.get(oldComponent);
    ChildComponentData newLocation = componentsDataMap.get(newComponent);

    if (oldLocation == null) {
        addComponent(newComponent);
    } else if (newLocation == null) {
        removeComponent(oldComponent);
        addComponent(newComponent, oldLocation.column1, oldLocation.row1,
                oldLocation.column2, oldLocation.row2);
    } else {
        int oldAlignment = oldLocation.alignment;
        oldLocation.alignment = newLocation.alignment;
        newLocation.alignment = oldAlignment;

        componentsDataMap.put(newComponent, oldLocation);
        componentsDataMap.put(oldComponent, newLocation);
    }
  }

//  public void setMargin(boolean enabled) {
//    setMargin(new MarginInfo(enabled));
//  }
//
//  public void setMargin(MarginInfo marginInfo) {
//    this.marginInfo = marginInfo;
//  }
//
//  public MarginInfo getMargin() {
//    if (marginInfo == null) {
//      marginInfo = new MarginInfo(false);
//    }
//    return marginInfo;
//  }
//
//  private void setMarginToElement() {
//    if (marginInfo == null) {
//      return;
//    }
//
//    toggleInternalStyle("v-margin-top", marginInfo.hasTop());
//    toggleInternalStyle("v-margin-bottom", marginInfo.hasBottom());
//    toggleInternalStyle("v-margin-left", marginInfo.hasLeft());
//    toggleInternalStyle("v-margin-right", marginInfo.hasRight());
//  }
//
//  private void toggleInternalStyle(String style, boolean add) {
//    if (add) {
//      addInternalStyles(style);
//    } else {
//      removeInternalStyles(style);
//    }
//  }
//
//  public void setSpacing(boolean spacing) {
//    toggleInternalStyle("v-spacing", spacing);
//  }
//
//  @Override
//  public void beforeClientResponse(boolean initial) {
//    // needs to be called before, so it adds/removes
//    // the internal style
//    setMarginToElement();
//    super.beforeClientResponse(initial);
//  }

  public Alignment getDefaultComponentAlignment() {
    return defaultComponentAlignment;
  }

  public void setDefaultComponentAlignment(Alignment defaultAlignment) {
    defaultComponentAlignment = defaultAlignment;
  }

  public Alignment getComponentAlignment(Component childComponent) {
    ChildComponentData childComponentData = componentsDataMap.get(childComponent);
    if (childComponentData == null) {
        throw new IllegalArgumentException(
                "The given component is not a child of this layout");
    } else {
        return new Alignment(childComponentData.alignment);
    }
   
  }

  public void setComponentAlignment(Component childComponent, Alignment alignment) {
    ChildComponentData childComponentData = componentsDataMap.get(childComponent);
    if (childComponentData == null) {
        throw new IllegalArgumentException(
                "Component must be added to layout before using setComponentAlignment()");
    } else {
        if (alignment == null) {
        childComponentData.alignment = ALIGNMENT_DEFAULT.getBitMask();
    } else {
        childComponentData.alignment = alignment.getBitMask();
    }
    }
  }

  private static class ChildComponentData implements Serializable {
    private int column1;
    private int row1;
    private int column2;
    private int row2;
    private int alignment = Alignment.TOP_LEFT.getBitMask();
    
    public ChildComponentData(int column1, int row1, int column2, int row2) {
      this.column1 = column1;
      this.row1 = row1;
      this.column2 = column2;
      this.row2 = row2;
    }

  }
}
