# Virtual QuickSort

## Overview
**Virtual QuickSort** implements a modified Quicksort algorithm to sort records directly on a binary file. Each record contains two 2-byte integers: a key (for sorting) and a data value. Sorting is performed using a buffer pool for managing file blocks (4096 bytes each) with an LRU replacement scheme.

---

## Features

### 1. Data Structure
- **Binary File Records**:
  - 4-byte records (2 bytes for the key, 2 bytes for the data).
  - Sorted in ascending order by the key field.

### 2. Operations
- **Buffer Pool Management**:
  - Uses the Least Recently Used (LRU) replacement scheme to manage 4096-byte blocks.
  - Mediates all file access for efficient disk I/O.
- **Sorting**:
  - A modified Quicksort algorithm where the array resides on disk instead of memory.
  - Optimized for performance with pivot selection and hybrid sorting for small datasets.

---

## Design Considerations

### Buffer Pool
- **Interaction**: Facilitates Quicksort operations by abstracting disk access as array-like interactions.
- **Implementation**: Uses a message-passing approach to handle byte arrays, ensuring efficient memory usage and reducing unnecessary disk reads/writes.

### Quicksort Modifications
- **Pivot Selection**:
  - Samples a small subset of records to choose an optimal pivot for fewer comparisons.
- **Hybrid Sorting**:
  - Switches to simpler sorting methods (e.g., Insertion Sort) for small datasets to enhance performance.
- **Efficiency**:
  - Minimized disk visits through strategic use of the buffer pool.
  - Balances sorting complexity with practical I/O constraints for timed grading.

---

## Getting Started

### Prerequisites
- **Java Development Kit (JDK)**: Version 8 or later.
- Binary data file in the specified format (multiple of 4096 bytes).

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/VirtualQuickSort.git
   ```
2. Navigate to the project directory:
   ```bash
   cd VirtualQuickSort
   ```
3. Compile the source code:
   ```bash
   javac -d bin src/**/*.java
   ```
4. Run the program:
   ```bash
   java -cp bin Main <input-file> <output-file>
   ```

---

## Usage

### Input Format
- Binary file containing 4-byte records:
  - 2-byte key (used for sorting).
  - 2-byte data value.

### Output
- A binary file with records sorted in ascending order by key.

### Commands
- **Sorting**: 
  ```bash
  java -cp bin Main input.dat sorted.dat
  ```
- **Debugging**:
  Enable logging to track buffer pool operations and performance.

---

## Project Structure
```
VirtualQuickSort/
├── src/
│   ├── QuickSort.java
│   ├── BufferPool.java
│   ├── Record.java
│   ├── Main.java
│   └── ... (other supporting files)
├── bin/
├── README.md
└── LICENSE
```

---

## Optimizations
- Hybrid sorting for small datasets.
- Efficient pivot selection using sampling.
- Reduced unnecessary disk I/O through smart buffer management.

---

## Future Enhancements
- Add visualization for buffer pool operations.
- Experiment with alternative replacement schemes.
- Explore external sorting techniques for larger datasets.

---

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Acknowledgments
- **OpenDSA Modules**: Concepts of buffer pools and efficient sorting algorithms.
- Algorithm design inspired by textbook implementations and real-world optimizations.
