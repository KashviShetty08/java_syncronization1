
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

// Readers-Writers problem using semaphores
public class ReadersWritersSemaphore {

    static int readCount = 0;
    static int sharedData = 0;

    // mutex protects the reader count; resource protects shared data
    static Semaphore mutex = new Semaphore(1);
    static Semaphore resource = new Semaphore(1);

    static List<String> events = Collections.synchronizedList(
        new ArrayList<>()
    );

    static Set<String> activeReaders = new HashSet<>();
    static Set<String> activeWriters = new HashSet<>();

    static long startTime;

    // Record each event so HTML can replay the actual execution
    static synchronized void logEvent(
        String name, String action, String message
    ) {
        double time = (System.nanoTime() - startTime) / 1_000_000_000.0;

        String readers = String.join(", ", activeReaders);
        String writers = String.join(", ", activeWriters);

        String event = String.format(
            Locale.US,
            "{\"time\":%.3f,\"name\":\"%s\",\"action\":\"%s\","
            + "\"message\":\"%s\",\"readers\":\"%s\","
            + "\"writers\":\"%s\",\"data\":%d}",
            time, name, action, message, readers, writers, sharedData
        );

        events.add(event);
        System.out.println(message);
    }

    static class Reader extends Thread {
        int id;

        Reader(int id) {
            this.id = id;
        }

        public void run() {
            String name = "Reader " + id;

            try {
                logEvent(name, "waiting", name + " is waiting to read.");

                // Safely update the number of readers
                mutex.acquire();
                readCount++;

                // The first reader locks the resource against writers
                if (readCount == 1) {
                    resource.acquire();
                }

                activeReaders.add(name);
                logEvent(name, "reading", name + " started reading.");
                mutex.release();

                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1001));

                logEvent(name, "read",
                    name + " read shared data = " + sharedData + ".");

                // Finish reading and update the reader count
                mutex.acquire();
                activeReaders.remove(name);
                readCount--;

                // The last reader releases the resource
                if (readCount == 0) {
                    resource.release();
                }

                mutex.release();

                logEvent(name, "finished", name + " finished reading.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Writer extends Thread {
        int id;

        Writer(int id) {
            this.id = id;
        }

        public void run() {
            String name = "Writer " + id;

            try {
                logEvent(name, "waiting", name + " is waiting to write.");

                // Only one writer can access the resource at a time
                resource.acquire();

                activeWriters.add(name);
                logEvent(name, "writing", name + " started writing.");

                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 801));

                sharedData++;

                logEvent(name, "updated",
                    name + " updated shared data to " + sharedData + ".");

                activeWriters.remove(name);
                logEvent(name, "finished", name + " finished writing.");

                resource.release();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Escape text before placing it inside JavaScript strings
    static String escape(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", " ");
    }

    static void generateHTML() throws IOException {
        String eventData = "[" + String.join(",", events) + "]";

        // The HTML page replays the events recorded above
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Readers-Writers Simulation</title>
            <style>
            body {font-family:Arial;background:#edf1f5;color:#18324b;padding:20px}
            .card {background:white;padding:20px;margin:18px auto;max-width:950px;border-radius:15px}
            button {background:#216ca8;color:white;border:0;padding:12px;margin:4px;cursor:pointer}
            .person {display:inline-block;background:#5795d5;color:white;padding:18px;margin:8px;border-radius:12px;transition:.3s}
            .active {background:#38a56a}
            .waiting {background:#e5a13b}
            #track {height:10px;background:#ddd}
            #bar {height:10px;background:#216ca8;width:0}
            table {width:100%;border-collapse:collapse}
            td,th {border:1px solid #ddd;padding:8px;text-align:left}
            </style>
            </head>
            <body>
            <div class="card">
            <h1>Readers-Writers Problem</h1>
            <p>Semaphore-Based Synchronization</p>
            <h2 id="message">Press Play to start</h2>
            <p id="data">Shared data: 0</p>
            <button onclick="play()">Play</button>
            <button onclick="pause()">Pause</button>
            <button onclick="next()">Next Event</button>
            <button onclick="reset()">Reset</button>
            <div id="track"><div id="bar"></div></div>
            <h3>Readers and Writers</h3>
            <div id="people"></div>
            </div>
            <div class="card">
            <h2>Execution Event Log</h2>
            <table><thead><tr><th>Time</th><th>Thread</th><th>Action</th><th>Message</th></tr></thead>
            <tbody id="logs"></tbody></table>
            </div>
            <script>
            const events = EVENT_DATA;
            let i = 0, timer = null;

            function showEvent() {
              if (i >= events.length) { pause(); return; }
              const e = events[i++];
              document.getElementById("message").textContent =
                "Event " + i + ": " + e.message;
              document.getElementById("data").textContent =
                "Shared data: " + e.data;

              // Update each reader or writer's visual state
              const people = document.getElementById("people");
              people.innerHTML = "";
              const names = ["Reader 1","Reader 2","Reader 3",
                             "Reader 4","Writer 1","Writer 2","Writer 3"];
              names.forEach(name => {
                const p = document.createElement("span");
                p.className = "person";
                p.textContent = name;
                if (e.readers.includes(name)) p.classList.add("active");
                if (e.writers.includes(name)) p.classList.add("active");
                if (e.name === name && e.action === "waiting")
                  p.classList.add("waiting");
                people.appendChild(p);
              });

              const row = document.createElement("tr");
              [e.time,e.name,e.action,e.message].forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
              });
              document.getElementById("logs").appendChild(row);
              document.getElementById("bar").style.width =
                (i / events.length * 100) + "%";
            }

            function play() {
              if (timer === null && i < events.length)
                timer = setInterval(showEvent, 800);
            }
            function pause() {
              clearInterval(timer);
              timer = null;
            }
            function next() { pause(); showEvent(); }
            function reset() {
              pause(); i = 0;
              document.getElementById("logs").innerHTML = "";
              document.getElementById("people").innerHTML = "";
              document.getElementById("message").textContent = "Press Play to start";
              document.getElementById("data").textContent = "Shared data: 0";
              document.getElementById("bar").style.width = "0";
            }
            </script>
            </body>
            </html>
            """.replace("EVENT_DATA", eventData);

        Path output = Path.of("readers_writers_semaphore.html");
        Files.writeString(output, html);

        System.out.println("HTML created: " + output.toAbsolutePath());

        // Open the simulation in the default browser
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(output.toUri());
        }
    }

    public static void main(String[] args) throws Exception {
        startTime = System.nanoTime();

        List<Thread> threads = new ArrayList<>();

        // Create four readers and three writers
        for (int i = 1; i <= 4; i++)
            threads.add(new Reader(i));

        for (int i = 1; i <= 3; i++)
            threads.add(new Writer(i));

        // Start threads in a random order
        Collections.shuffle(threads);
        for (Thread thread : threads) {
            thread.start();
            Thread.sleep(80);
        }

        // Wait for all threads to finish
        for (Thread thread : threads)
            thread.join();

        generateHTML();

        System.out.println("Final shared data: " + sharedData);
        System.out.println("Total events: " + events.size());
    }
}
