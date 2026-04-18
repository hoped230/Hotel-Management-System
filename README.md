 HEAD
# Hotel-Management-System
JavaFX Practice Project

# OSDL Hotel Management

JavaFX hotel management project aligned to Weeks 1 to 9 of the pasted manual.

## Lab features covered

- Week 1: classes, objects, encapsulation, inheritance, abstraction, interfaces, constructor overloading, method overriding, `this` and `super`
- Week 2: wrapper classes, autoboxing/unboxing, enum constructor, enum methods
- Week 3: thread creation using `Thread` and `Runnable`, `sleep()`, `yield()`, `join()`
- Week 4: synchronization, synchronized block, `wait()`, `notifyAll()`
- Week 5: `FileInputStream`, `FileOutputStream`, `FileReader`, `FileWriter`
- Week 6: `RandomAccessFile`, serialization and deserialization
- Week 7: generic class, bounded type, generic method, generic array method, pair class
- Week 8: `ArrayList`, `HashMap`, `Iterator`, `Collections.sort()`
- Week 9: JavaFX GUI with `Label`, `TextField`, `Button`, `ComboBox`, `TableView`, `GridPane`, `VBox`, `HBox`, alerts, and event handling

## Run

```bash
mvn javafx:run
```

## Run modular backup version

```bash
mvn -Pmodular javafx:run
```

## Project data

- Room records are stored with `RandomAccessFile` in `data/rooms.dat`
- Bookings are stored in `data/bookings.txt`
- Services are stored in `data/services.txt`
- Serialized booking objects are stored in `data/bookings.ser`
- Generated invoices and byte-stream copies are stored in `invoices/`
6da9eae (Initial Commit)
