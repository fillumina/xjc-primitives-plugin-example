# xjc-primitives-plugin-example

An example of [`xjc-primitives-plugin`](https://github.com/fillumina/xjc-primitives-plugin) inside a
real build, and the functional test of that wiring. The `jaxb-maven-plugin` runs the plugin over
`src/main/xsd/meter.xsd`, the generated sources are compiled by the same build, and a test reads the
types that came out.

```xml
<plugin>
  <groupId>org.jvnet.jaxb</groupId>
  <artifactId>jaxb-maven-plugin</artifactId>
  <version>4.0.9</version>
  <executions>
    <execution>
      <goals>
        <goal>generate</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <extension>true</extension>
    <schemaDirectory>${project.basedir}/src/main/xsd</schemaDirectory>
    <args>
      <arg>-p</arg>
      <arg>com.example.meter</arg>
      <arg>-XReplacePrimitives</arg>
      <arg>-XReplacePrimitives:exclude=*#scale</arg>
    </args>
    <plugins>
      <plugin>
        <groupId>com.fillumina</groupId>
        <artifactId>xjc-primitives-plugin</artifactId>
        <version>${xjc-primitives-plugin.version}</version>
      </plugin>
    </plugins>
  </configuration>
</plugin>
```

Both arguments are needed: the bare option activates the plugin, the one carrying a value
configures it. The `exclude` selector in this build leaves the `scale` field a primitive, so the
example shows the option and the contrast it makes, and the test checks both sides.

## What the schema makes the plugin write

`meter.xsd` is a small schema whose fields are the primitive types a real one uses. XJC generates a
primitive for every required element of a primitive type, and the plugin replaces it with the boxed
class, following the getter and the setter:

| in the schema | what XJC generates | what the plugin generates |
| --- | --- | --- |
| `xs:long` | `protected long reading;` | `protected Long reading;` |
| `xs:short` | `protected short precision;` | `protected Short precision;` |
| `xs:double` | `protected double factor;` | `protected Double factor;` |
| `xs:float` | `protected float offset;` | `protected Float offset;` |
| `xs:boolean`, element or attribute | `protected boolean active;` | `protected Boolean active;` |
| a field an `exclude` selector names | `protected byte scale;` | `protected byte scale;`, unchanged |

The reason is that a primitive cannot be absent: without the boxed type, a field the document never
carried is indistinguishable from a zero or a `false`, and `@NotNull` on it can never fail. The
boxed class makes the absence a `null` the constraint can report.

## Building

The build needs JDK 21 and Maven, and nothing else:

```
mvn -B verify -Dxjc-primitives-plugin.version=<version>
```

The version is a property, so the example runs against a release from Maven Central and against a
snapshot without being touched. The snapshot side needs `mvn install` in the project itself first:

```
cd ../xjc-primitives-plugin && mvn -B install
cd ../xjc-primitives-plugin-example && mvn -B verify -Dxjc-primitives-plugin.version=1.0.0-SNAPSHOT
```

## What the test proves, and what it does not

`ThePrimitivesAreBoxedTest` reads the types from the compiled generated class: the six fields that
were primitives are the boxed classes, the getter and the setter followed them, the `exclude`
selector left `scale` a `byte`, and a fresh instance reports `null` for a value the document did not
carry where a primitive would have reported zero.

It is the consumer path — the plugin reached through the codegen plugin of a real build — and not a
replacement for the test suite of the project, which is where the behaviour of the plugin itself is
pinned.

The three plugins of this line together in one build, which is where the split is shown to do what
the single plugin did, are in
[`xjc-plugins-example`](https://github.com/fillumina/xjc-plugins-example).
