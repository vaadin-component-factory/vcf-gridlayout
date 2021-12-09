# Grid Layout component for Vaadin Flow

This component recreates some of the functionalities of Vaadin 7 Grid Layout component for Vaadin Flow. 

This component is part of Vaadin Component Factory.

## Description 

Grid Layout component provides support to the following features:

- Create a GridLayout with predefined number of columns & rows
- Set spacing & margin
- Add components 

## Development instructions

- Build the project and install the add-on locally:
```
mvn clean install
```
- For starting the demo server go to vcf-gridlayout-demo and run:
```
mvn jetty:run
```
This deploys demo at http://localhost:8080

## How to use it 

Just create a Grid Layout component in the same way as in Vaadin 7.

For example:

```java
GridLayout grid = new GridLayout(4, 4);
```

## Missing features or bugs

You can report any issue or missing feature on [GitHub](https://github.com/vaadin-component-factory/vcf-gridlayout/issues).

## License

Apache License 2.0.
