package com.vaadin.componentfactory.gridlayout;

/*-
 * #%L
 * GridLayout
 * %%
 * Copyright (C) 2021 Vaadin Ltd
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Unit;
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
import java.util.Map.Entry;
import java.util.stream.Collectors;

@CssImport("./styles/grid-layout.css")
public class GridLayout extends Composite<Div> implements HasSize, HasStyle {

  private static final String SLOT_CLASS_NAME = "v-slot";

  private static final String SPACING_CLASS_NAME = "v-spacing";

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

  /**
   * Map of the components with data about the specified area in the grid layout.
   */
  private final Map<Component, ChildComponentData> componentsDataMap = new HashMap<>();

  /**
   * Cursor X position: this is where the next component with unspecified x,y is inserted
   */
  private int cursorX = 0;

  /**
   * Cursor Y position: this is where the next component with unspecified x,y is inserted
   */
  private int cursorY = 0;
  
  private ArrayList<String> colWidths= null;
  private ArrayList<Float> colWidthRatios= null;

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
   * @param columns Number of columns in the grid.
   * @param rows Number of rows in the grid.
   */
  public GridLayout(int columns, int rows) {
    getElement().getClassList().add("v-gridlayout");
    getElement().getStyle().set("display", "grid");
    setColumns(columns);
    setRows(rows);
  }

  /**
   * Sets the number of columns in the grid. The column count can not be reduced if there are any
   * areas that would be outside of the shrunk grid.
   *
   * @param columns the new number of columns in the grid layout.
   */
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

    // Checks for overlaps
    if (getColumns() > columns) {
      for (Entry<Component, ChildComponentData> entry : componentsDataMap.entrySet()) {
        if (entry.getValue().column2 >= columns) {
          throw new OutOfBoundsException(new Area(entry.getValue(), (Component) entry.getKey()));
        }
      }
    }

    if (colWidths == null)
    {
        // Use auto width for all columns
        colWidths = new ArrayList<>();
        colWidths.addAll(Collections.nCopies(columns, "auto"));
    }
    else
    {
        ArrayList<String> oldColWidth= colWidths;
        colWidths = new ArrayList<>();
        for (int i= 0; i < 0; i++)
        {
            if (oldColWidth.size() > i)
            {
                colWidths.add(oldColWidth.get(i));
            }
            else
            {
                colWidths.addAll(Collections.nCopies(columns, "auto"));
            }
        }
    }
    String gridColumns = colWidths.stream().collect(Collectors.joining(" "));
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

  /**
   * Sets the number of rows in the grid. The number of rows can not be reduced if there are any
   * areas that would be outside of the shrunk grid.
   *
   * @param rows the new number of rows in the grid layout.
   */
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

    // Checks for overlaps
    if (getRows() > rows) {
      for (Entry<Component, ChildComponentData> entry : componentsDataMap.entrySet()) {
        if (entry.getValue().row2 >= rows) {
          throw new OutOfBoundsException(new Area(entry.getValue(), (Component) entry.getKey()));
        }
      }
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

  /**
   * Moves the cursor forward by one. If the cursor goes out of the right grid border, it is moved
   * to the first column of the next row.
   */
  public void space() {
    cursorX++;
    if (cursorX >= getColumns()) {
      cursorX = 0;
      cursorY++;
    }
  }

  /**
   * Adds the component into this container to the cursor position. If the
   * cursor position is already occupied, the cursor is moved forwards to find
   * free position. If the cursor goes out from the bottom of the grid, the
   * grid is automatically extended.
   *
   * @param component
   *            the component to be added, not <code>null</code>.
   */
  public void addComponent(Component component) {
    if (component == null) {
      throw new IllegalArgumentException("Component must not be null");
    }

    // Finds first available place from the grid
    Area area;
    boolean done = false;
    while (!done) {
      try {
        area = new Area(component, cursorX, cursorY, cursorX, cursorY);
        checkExistingOverlaps(area);
        done = true;
      } catch (final OverlapsException e) {
        space();
      }
    }

    // Extends the grid if needed
    if (cursorX >= columns) {
      setColumns(cursorX + 1);
    }
    if (cursorY >= rows) {
      setRows(cursorY + 1);
    }

    addComponent(component, cursorX, cursorY);
  }

  /**
   * @return Returns the current x-position (column) of the cursor.
   */
  public int getCursorX() {
    return cursorX;
  }

  /**
   * @return Returns the current y-position (row) of the cursor.
   */
  public int getCursorY() {
    return cursorY;
  }

  /**
   * Adds the component to the grid in cells column1,row1 (NortWest corner of
   * the area.) End coordinates (SouthEast corner of the area) are the same as
   * column1,row1. The coordinates are zero-based. Component width and height
   * is 1.
   *
   * @param component
   *            the component to be added, not <code>null</code>.
   * @param column
   *            the column index, starting from 0.
   * @param row
   *            the row index, starting from 0.
   * @throws OverlapsException
   *             if the new component overlaps with any of the components
   *             already in the grid.
   * @throws OutOfBoundsException
   *             if the cell is outside the grid area.
   */
  public void addComponent(Component component, int column, int row) 
      throws OverlapsException, OutOfBoundsException{
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
   * @throws OverlapsException if the new component overlaps with any of the components already in
   *         the grid.
   * @throws OutOfBoundsException if the cells are outside the grid area.
   */
  public void addComponent(Component component, int column1, int row1, int column2, int row2)
      throws OverlapsException, OutOfBoundsException {

    if (component == null) {
      throw new NullPointerException("Component must not be null");
    }

    // Checks that the component does not already exist in the container
    if (components.contains(component)) {
      throw new IllegalArgumentException("Component is already in the container");
    }

    // Creates the area
    final Area area = new Area(component, column1, row1, column2, row2);

    // Checks the validity of the coordinates
    if (column2 < column1 || row2 < row1) {
      throw new IllegalArgumentException("Illegal coordinates for the component");
    }

    if (column1 < 0 || row1 < 0 || column2 >= getColumns() || row2 >= getRows()) {
      throw new OutOfBoundsException(area);
    }

    // Checks that newItem does not overlap with existing items
    checkExistingOverlaps(area);

    // Inserts the component to right place at the list
    // Respect top-down, left-right ordering
    final Iterator<Component> i = components.iterator();
    component = wrapComponent(component);
    int index = 0;
    boolean done = false;
    while (!done && i.hasNext()) {
      final ChildComponentData existingArea = componentsDataMap.get(i.next());
      if ((existingArea.row1 >= row1 && existingArea.column1 > column1)
          || existingArea.row1 > row1) {
        components.add(index, component);
        setComponentArea(component, column1, row1, column2, row2);
        done = true;
      }
      index++;
    }
    if (!done) {
      components.addLast(component);
      setComponentArea(component, column1, row1, column2, row2);
    }

    componentsDataMap.put(component, new ChildComponentData(column1, row1, column2, row2));

    // Attempt to add to div
    try {
      div.removeAll();
      div.add(components.toArray(new Component[0]));
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
   */
  private void setComponentArea(Component component, int column1, int row1, int column2, int row2) {
    component.getElement().getStyle().set("grid-row-start", String.valueOf(row1 + 1));
    component.getElement().getStyle().set("grid-row-end", String.valueOf(row2 + 2));
    component.getElement().getStyle().set("grid-column-start", String.valueOf(column1 + 1));
    component.getElement().getStyle().set("grid-column-end", String.valueOf(column2 + 2));
  }

  private Div wrapComponent(Component component) {
    Div divWrapper = new Div();
    divWrapper.getElement().getClassList().add(SLOT_CLASS_NAME);
    divWrapper.add(component);
    return divWrapper;
  }

  /**
   * Tests if the given area overlaps with any of the items already on the grid.
   *
   * @param area the Area to be checked for overlapping.
   * @throws OverlapsException if <code>area</code> overlaps with any existing area.
   */
  private void checkExistingOverlaps(Area area) throws OverlapsException {
    for (Entry<Component, ChildComponentData> entry : componentsDataMap.entrySet()) {
      if (componentsOverlap(entry.getValue(), area.childData)) {
        // Component not added, overlaps with existing component
        throw new OverlapsException(new Area(entry.getValue(), (Component) entry.getKey()));
      }
    }
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
   * Gets the Component at given index.
   *
   * @param x The column index, starting from 0 for the leftmost column.
   * @param y The row index, starting from 0 for the topmost row.
   * @return Component in given cell or null if empty
   */
  public Component getComponent(int x, int y) {
    for (Entry<Component, ChildComponentData> entry : componentsDataMap.entrySet()) {
      ChildComponentData childData = entry.getValue();
      if (childData.column1 <= x && x <= childData.column2 && childData.row1 <= y
          && y <= childData.row2) {
        return (Component) entry.getKey();
      }
    }
    return null;
  }

  /**
   * Enables margins for grid layout.
   * 
   * @param enabled true if margins of grid layout should be enabled, false otherwise.
   */
  public void setMargin(boolean enabled) {
    setMargin(new MarginInfo(enabled));
  }

  /**
   * Enables margins for grid layout
   * 
   * @param marginInfo object that contains the new margins.
   */
  public void setMargin(MarginInfo marginInfo) {
    this.marginInfo = marginInfo;

    div.getElement().getClassList().set("v-margin-top", marginInfo.hasTop());
    div.getElement().getClassList().set("v-margin-bottom", marginInfo.hasBottom());
    div.getElement().getClassList().set("v-margin-left", marginInfo.hasLeft());
    div.getElement().getClassList().set("v-margin-right", marginInfo.hasRight());
  }

  /**
   * @return Returns margin info.
   */
  public MarginInfo getMargin() {
    if (marginInfo == null) {
      marginInfo = new MarginInfo(false);
    }
    return marginInfo;
  }

  /**
   * Enables spacing between components within the grid layout.
   * 
   * @param spacing true if spacing should be enabled, false otherwise.
   */
  public void setSpacing(boolean spacing) {
    div.getElement().getClassList().set(SPACING_CLASS_NAME, spacing);
  }
  
  /**
   * Sets the expand ratio of given column.
   * The expand ratio defines how excess space is distributed among columns. 
   * Excess space means space that is left over from components that are not 
   * sized relatively. By default, the excess space is distributed evenly.
   * Note, that width of this GridLayout needs to be defined (fixed or relative, 
   * as opposed to undefined height) for this method to have any effect.
   * Note that checking for relative width for the child components is done 
   * on the server so you cannot set a child component to have undefined 
   * width on the server and set it to 100% in CSS. 
   * You must set it to 100% on the server.
   * 
   * @deprecated Since the used css grid calculates widths differently
   * user the new {@link #setColumnWidth(int,String)} instead
   *
   * @param columnIndex 1 based column number
   * @param ratio expand ratio, please use parts of 100%
   */
  public void setColumnExpandRatio(int columnIndex,
                                 float ratio)  {
    if (columnIndex < 1) {
      throw new IllegalArgumentException(
          "The number of columns and rows in the grid must be at least 1");
    }

    // Checks for overlaps
    if (getColumns() < columnIndex) {
      throw new IllegalArgumentException(
          "The number of columns is less than "+columnIndex);
    }
    if (colWidthRatios == null) {
        colWidthRatios= new ArrayList<>();
        colWidthRatios.addAll(Collections.nCopies(columns, 0.0f));
    }
    colWidthRatios.set(columnIndex-1, ratio);
    float totalWidth= 0;
    for (Float wr : colWidthRatios)
    {
        if (wr != null)
        {
            totalWidth+= wr;
        }
    }
    int myCol= 0;
    for (Float wr : colWidthRatios)
    {
        myCol++;
        if (wr != null)
        {
            float percent= wr / totalWidth * 100;
            setColumnWidth(myCol, Float.toString(percent)+"%");
        }
        else
        {
            setColumnWidth(myCol, "auto");
        }
    }
  }

  /**
   * Set the column width.
   * If you use % widths, make sure to disable margings and paddings when the
   * total width gives 100%
   * 
   * @param columnIndex 1 based column number
   * @param colWidth column with numeric value
   * @param widthUnit width unit
   */
  public void setColumnWidth(int columnIndex,
                             float colWidth,
                             Unit widthUnit)  {
      setColumnWidth(columnIndex, Float.toString(colWidth)+widthUnit.getSymbol());
    }
  
  /**
   * Set the column width.
   * If you use % widths, make sure to disable margings and paddings when the
   * total width gives 100%
   * 
   * @param columnIndex 1 based column number
   * @param colWidth Can be a width in em/px etc. or a relative width like 33%
   */
  public void setColumnWidth(int columnIndex,
                             String colWidth)  {
    if (columnIndex < 1) {
      throw new IllegalArgumentException(
          "The number of columns and rows in the grid must be at least 1");
    }

    // Checks for overlaps
    if (getColumns() < columnIndex) {
      throw new IllegalArgumentException(
          "The number of columns is less than "+columnIndex);
    }

    colWidths.set(columnIndex-1, colWidth);
    String gridColumns = colWidths.stream().collect(Collectors.joining(" "));
    getElement().getStyle().set("grid-template-columns", gridColumns);
  }
  
  /**
   * Defines a rectangular area of cells in a GridLayout.
   *
   * <p>
   * Also maintains a reference to the component contained in the area.
   * </p>
   *
   * <p>
   * The area is specified by the cell coordinates of its upper left corner (column1,row1) and lower
   * right corner (column2,row2). As otherwise with GridLayout, the column and row coordinates start
   * from zero.
   * </p>
   *
   * @author Vaadin Ltd.
   * @since 3.0
   */
  public class Area implements Serializable {
    private final ChildComponentData childData;
    private final Component component;

    /**
     * <p>
     * Construct a new area on a grid.
     * </p>
     *
     * @param component the component connected to the area.
     * @param column1 The column of the upper left corner cell of the area. The leftmost column has
     *        index 0.
     * @param row1 The row of the upper left corner cell of the area. The topmost row has index 0.
     * @param column2 The column of the lower right corner cell of the area. The leftmost column has
     *        index 0.
     * @param row2 The row of the lower right corner cell of the area. The topmost row has index 0.
     */
    public Area(Component component, int column1, int row1, int column2, int row2) {
      this.component = component;
      childData = new ChildComponentData();
      childData.alignment = Alignment.TOP_LEFT.getBitMask();
      childData.column1 = column1;
      childData.row1 = row1;
      childData.column2 = column2;
      childData.row2 = row2;
    }

    public Area(ChildComponentData childData, Component component) {
      this.childData = childData;
      this.component = component;
    }

    /**
     * Tests if this Area overlaps with another Area.
     *
     * @param other the other Area that is to be tested for overlap with this area
     * @return <code>true</code> if <code>other</code> area overlaps with this on,
     *         <code>false</code> if it does not.
     */
    public boolean overlaps(Area other) {
      return componentsOverlap(childData, other.childData);
    }

    /**
     * Gets the component connected to the area.
     *
     * @return the Component.
     */
    public Component getComponent() {
      return component;
    }

    /**
     * Gets the column of the top-left corner cell.
     *
     * @return the column of the top-left corner cell.
     */
    public int getColumn1() {
      return childData.column1;
    }

    /**
     * Gets the column of the bottom-right corner cell.
     *
     * @return the column of the bottom-right corner cell.
     */
    public int getColumn2() {
      return childData.column2;
    }

    /**
     * Gets the row of the top-left corner cell.
     *
     * @return the row of the top-left corner cell.
     */
    public int getRow1() {
      return childData.row1;
    }

    /**
     * Gets the row of the bottom-right corner cell.
     *
     * @return the row of the bottom-right corner cell.
     */
    public int getRow2() {
      return childData.row2;
    }

  }

  private static boolean componentsOverlap(ChildComponentData a, ChildComponentData b) {
    return a.column1 <= b.column2 && a.row1 <= b.row2 && a.column2 >= b.column1 && a.row2 >= b.row1;
  }

  /**
   * Gridlayout does not support laying components on top of each other. An
   * <code>OverlapsException</code> is thrown when a component already exists (even partly) at the
   * same space on a grid with the new component.
   *
   * @author Vaadin Ltd.
   * @since 3.0
   */
  public class OverlapsException extends java.lang.RuntimeException {

    private final Area existingArea;

    /**
     * Constructs an <code>OverlapsException</code>.
     *
     * @param existingArea existing area
     */
    public OverlapsException(Area existingArea) {
      this.existingArea = existingArea;
    }

    @Override
    public String getMessage() {
      StringBuilder sb = new StringBuilder();
      Component component = existingArea.getComponent();
      sb.append(component);
      sb.append("( type = ");
      sb.append(component.getClass().getName());
      sb.append(")");
      sb.append(" is already added to ");
      sb.append(existingArea.childData.column1);
      sb.append(",");
      sb.append(existingArea.childData.column1);
      sb.append(",");
      sb.append(existingArea.childData.row1);
      sb.append(",");
      sb.append(existingArea.childData.row2);
      sb.append("(column1, column2, row1, row2).");

      return sb.toString();
    }

    /**
     * Gets the area .
     *
     * @return the existing area.
     */
    public Area getArea() {
      return existingArea;
    }
  }

  /**
   * An <code>Exception</code> object which is thrown when an area exceeds the bounds of the grid.
   *
   * @author Vaadin Ltd.
   * @since 3.0
   */
  public class OutOfBoundsException extends java.lang.RuntimeException {

    private final Area areaOutOfBounds;

    /**
     * Constructs an <code>OoutOfBoundsException</code> with the specified detail message.
     *
     * @param areaOutOfBounds area out of bounds
     */
    public OutOfBoundsException(Area areaOutOfBounds) {
      this.areaOutOfBounds = areaOutOfBounds;
    }

    /**
     * Gets the area that is out of bounds.
     *
     * @return the area out of Bound.
     */
    public Area getArea() {
      return areaOutOfBounds;
    }
  }

  private static class ChildComponentData implements Serializable {
    private int column1;
    private int row1;
    private int column2;
    private int row2;
    private int alignment = Alignment.TOP_LEFT.getBitMask();

    public ChildComponentData() {}

    public ChildComponentData(int column1, int row1, int column2, int row2) {
      this.column1 = column1;
      this.row1 = row1;
      this.column2 = column2;
      this.row2 = row2;
    }

  }
}
