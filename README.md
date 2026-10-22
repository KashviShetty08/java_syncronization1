# Operating System Synchronization Assignment

**Student Name:** Kashvi B Shetty  
**USN/Registration Number:** NNM24IS103  
**Branch:** Information Science and Engineering (ISE)  
**Programming Language:** Java

## 1. Project Overview

This project demonstrates synchronization techniques used to manage concurrent threads in operating systems. It covers two classic synchronization problems: Readers-Writers and Dining Philosophers. Each problem is implemented using semaphores and monitors, with HTML visualizations used to represent program execution.

## 2. Implementations

### 2.1 Readers-Writers Using Semaphores

This implementation uses Java's `Semaphore` class to coordinate readers and writers accessing shared data. A mutex semaphore protects the reader count, while a resource semaphore controls access to the shared resource. Multiple readers can access the data simultaneously, whereas a writer requires exclusive access.

### 2.2 Readers-Writers Using Monitors

This implementation uses Java's synchronization mechanisms to coordinate readers and writers. It maintains a reader count and controls access to shared data using synchronized methods or blocks. The implementation helps prevent conflicting read and write operations.

### 2.3 Dining Philosophers Using Semaphores

This implementation uses Java semaphores to coordinate philosophers competing for forks. Semaphores control access to shared forks, while an additional room semaphore limits the number of philosophers who can compete for forks at the same time. This approach helps prevent deadlock.

### 2.4 Dining Philosophers Using Monitors

This implementation uses Java's monitor-based synchronization through synchronized methods and shared state. It tracks which philosophers are eating and coordinates access to neighboring forks. A philosopher can eat only when the required forks are available, preventing neighboring philosophers from eating simultaneously.

## 3. Programming Language and Requirements

- Java Development Kit (JDK)
- Java standard library classes, including `java.util`, `java.util.concurrent.Semaphore`, and file-handling utilities where applicable.
- HTML, CSS, and JavaScript for visualizations.
- A web browser to view the HTML simulations.
- Visual Studio Code or another Java-compatible editor.

## 4. Project Structure

```text
OS-Synchronization-Assignment/
├── ReadersWritersSemaphore.java
├── ReadersWritersMonitor.java
├── DiningPhilosophersSemaphore.java
├── DiningPhilosophersMonitor.java
├── readers_writers_semaphore.html
├── readers_writers_monitor.html
├── dining_philosophers_semaphore.html
├── dining_philosophers_monitor.html
├── README.md
├── screenshots/
└── Assignment_Report.pdf
```

*Note: The structure above is a suggested layout. Adjust the filenames and folders to match your actual project.*

## 5. Execution Instructions

1. Install a compatible JDK and ensure Java is available in the system PATH.
2. Open the project folder in Visual Studio Code.
3. Open the integrated terminal in the directory containing the Java source files.
4. Run the required Java program using the commands below.

### Readers-Writers Using Semaphores

```powershell
java ReadersWritersSemaphore.java
```

### Readers-Writers Using Monitors

```powershell
java ReadersWritersMonitor.java
```

### Dining Philosophers Using Semaphores

```powershell
java DiningPhilosophersSemaphore.java
```

### Dining Philosophers Using Monitors

```powershell
java DiningPhilosophersMonitor.java
```

These commands use Java's source-file execution feature, so a separate manual `javac` command is not required. The filenames must match the actual source files.

## 6. Generating the HTML Simulations

Run each Java implementation according to its execution instructions. Where implemented, the program records synchronization events and writes them to an HTML file for visualization.

Ensure that the output paths and HTML filenames match those used in the Java source code. If the program generates HTML files automatically, check that the files are created successfully after execution.

## 7. Viewing the HTML Simulations

1. Locate the generated HTML file in the project folder.
2. Open the file in a web browser.
3. Observe the synchronization events displayed in the simulation.
4. Use any available playback controls to inspect the recorded execution.

The available visualization features depend on the corresponding HTML implementation.

## 8. Expected Output and Screenshots

The programs demonstrate synchronization between concurrent readers and writers and between philosophers competing for shared forks.

Expected behavior includes:

- Multiple readers accessing shared data under the appropriate synchronization rules.
- Writers obtaining exclusive access to shared data.
- Philosophers acquiring the necessary forks before eating.
- Release of shared resources after an operation is completed.
- HTML visualizations displaying recorded synchronization events, where supported.

Screenshots of actual program execution and HTML simulations can be stored in the `screenshots/` directory.

## 9. Known Limitations

- Thread execution order may vary between runs due to operating-system scheduling.
- The timing and order of recorded events may differ between executions.
- Starvation prevention depends on the synchronization strategy used.
- HTML simulations display recorded events and may not represent every internal scheduling detail.
- The visualizations depend on successful generation of the corresponding HTML files.

## 10. AI Tools and Prompt Log

**AI tool used:** ChatGPT (OpenAI).

The AI tool was used for assistance with understanding synchronization concepts, explaining Java code, organizing project documentation, and preparing the README file.

The use of AI assistance should be acknowledged according to the assignment instructions. Any required prompt log should contain the prompts actually used during the project.

## 11. References and Acknowledgements

- Operating Systems course materials and assignment instructions.
- Java documentation: https://docs.oracle.com/en/java/
- Java concurrency documentation: https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/util/concurrent/package-summary.html

AI assistance is acknowledged for the tasks described above. The implementation details, execution results, screenshots, and conclusions should correspond to the actual project.

