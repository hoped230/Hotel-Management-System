 HEAD
# Hotel-Management-System
JavaFX Practice Project

# Hotel Management

JavaFX hotel management project

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
